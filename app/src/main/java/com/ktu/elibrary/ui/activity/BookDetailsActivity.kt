package com.ktu.elibrary.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.ktu.elibrary.R
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.databinding.ActivityBookDetailsBinding
import com.ktu.elibrary.ui.workmanager.DownloadWorker

class BookDetailsActivity : AppCompatActivity() {

    private var mTitle : String = ""
    private var fileUrl : String = ""
    companion object{
        private const val BOOK = "book"
        fun newIntent(context: Context,pdf : PdfVo) : Intent{
            val intent = Intent(context,BookDetailsActivity::class.java)
            intent.putExtra(BOOK,pdf)
            return intent;
        }
    }

    private lateinit var mBinding : ActivityBookDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setUpToolbar()
        val pdfBook = intent.getParcelableExtra<PdfVo>(BOOK)
        if (pdfBook != null) {
            bindData(pdfBook)
        }else{
            Toast.makeText(this, "book is null", Toast.LENGTH_SHORT).show()
        }
        setUpListeners()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.left_arrow)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpListeners(){
        mBinding.btnDownload.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(this)
                .setTitle(mTitle)
                .setMessage("Are you sure to Download?")
                .setPositiveButton("OK"){dialog,_ ->
                    checkPermissionsAndDownload()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel"){dialog,_->
                    dialog.dismiss()
                }
            val alertDialog = dialogBuilder.create()
            alertDialog.show()
        }
    }

    private fun bindData(pdf: PdfVo){
        mTitle = pdf.title
        fileUrl = pdf.fileUrl
        mBinding.tvTitle.text = pdf.title
        mBinding.tvPages.text = pdf.pages
        mBinding.tvUploadTime.text = getTimeAgo(pdf.id.toLong())
        mBinding.tvLanguage.text = pdf.language
        mBinding.tvUploadUser.text = pdf.uploadUser
        Glide.with(this)
            .load(pdf.posterImage)
            .into(mBinding.ivPosterImage)
    }

    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val timeAgo = DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS)
        return timeAgo.toString()
    }

    private fun checkPermissionsAndDownload() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 11 (API 29) and later
            startDownload()
        } else {
            // For earlier versions, still check WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    100
                )
            } else {
                startDownload()
            }
        }
    }

    private fun startDownload(){
        val data = Data.Builder()
            .putString("url",fileUrl)
            .putString("title",mTitle)
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_ROAMING)
            .setRequiresStorageNotLow(true)
            .build()

        val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(constraints)
            .setInputData(data)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueue(downloadWorkRequest)

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(downloadWorkRequest.id)
            .observe(this) { workInfo ->
                if (workInfo != null) {
                    when (workInfo.state) {
                        WorkInfo.State.SUCCEEDED -> {

                        }
                        WorkInfo.State.FAILED -> {

                        }
                        WorkInfo.State.RUNNING -> {
                            Toast.makeText(this, "Downloading...", Toast.LENGTH_SHORT).show()
                            val progress = workInfo.progress.getInt("progress", 0)
                        }
                        else -> {}
                    }
                }
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDownload()
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }
}