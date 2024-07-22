package com.ktu.elibrary.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.PdfModel
import com.ktu.elibrary.data.model.PdfModelImpl
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.databinding.ActivityBookDetailsBinding
import com.ktu.elibrary.extensions.hide
import com.ktu.elibrary.extensions.show
import com.ktu.elibrary.ui.workmanager.DownloadWorker

class BookDetailsActivity : AppCompatActivity() {

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private val mPdfModel : PdfModel = PdfModelImpl
    private var bookId : String = ""
    private var majorId : Int = 0
    private var mTitle : String = ""
    private var fileUrl : String = ""
    private var mUserRole : Int = 0
    private var mId : String = ""
    companion object{
        private const val BOOK = "book"
        private const val ROLE = "role"
        private const val MAJOR = "major"
        fun newIntent(context: Context,pdf : PdfVo,role : Int,major : Int) : Intent{
            val intent = Intent(context,BookDetailsActivity::class.java)
            intent.putExtra(BOOK,pdf)
            intent.putExtra(ROLE,role)
            intent.putExtra(MAJOR,major)
            return intent;
        }
    }

    private lateinit var mBinding : ActivityBookDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setUpToolbar()
        mUserRole = intent.getIntExtra(ROLE,0)
        majorId = intent.getIntExtra(MAJOR,0)
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
            val dialogBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
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

        //delete does not have exception although the collection is not exit
        mBinding.fabDelete.setOnClickListener {
            if (bookId.isNotEmpty() && fileUrl.isNotEmpty()){
                mBinding.progressBar.show()
                mBinding.btnDownload.isEnabled = false
                mBinding.btnDownload.alpha = 0.5f
                val storagePath = extractStoragePathFromUrl(fileUrl)
                mPdfModel.deleteBook(
                    major = majorId,
                    bookId,
                    storagePath,
                    onSuccess = {
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                        finish()
                    },
                    onFailure = {
                        mBinding.progressBar.hide()
                        mBinding.btnDownload.isEnabled = true
                        mBinding.btnDownload.alpha = 1.0f
                        Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                    }

                )
            }
        }

        if (mUserRole == 2){
            mBinding.fabDelete.show()
        }
    }

    private fun bindData(pdf: PdfVo){
        bookId = pdf.id
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
        mBinding.btnDownload.isEnabled = false
        mBinding.btnDownload.alpha = 0.2f
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
                            mBinding.btnDownload.isEnabled = true
                            mBinding.btnDownload.alpha = 1.0f
                            logDownloadCompletionEvent(mTitle)
                        }
                        WorkInfo.State.FAILED -> {
                            mBinding.btnDownload.isEnabled = true
                            mBinding.btnDownload.alpha = 1.0f
                            Toast.makeText(this, "Download is not successful", Toast.LENGTH_SHORT).show()
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

    private fun logDownloadCompletionEvent(fileName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID,mId)
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, fileName)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE,"user")
        firebaseAnalytics.logEvent("download_complete", bundle)

    }

    fun extractStoragePathFromUrl(url: String): String {
        val uri = Uri.parse(url)
        val path = uri.path?.substringAfter("/o/")?.substringBefore("?alt=media")
        return path?.replace("%2F", "/") ?: ""
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