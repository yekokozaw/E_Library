package com.ktu.elibrary.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.PdfModel
import com.ktu.elibrary.data.model.PdfModelImpl
import com.ktu.elibrary.databinding.ActivityCreatePdfBinding
import com.ktu.elibrary.extensions.PDFUtils
import com.ktu.elibrary.extensions.SharedPreferencesHelper
import com.ktu.elibrary.extensions.hide
import com.ktu.elibrary.extensions.show
import java.io.File
import java.io.InputStream

class CreatePdfActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityCreatePdfBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private var isFileSelected = false
    private var mTempFile : File? = null
    private var mBitmap : Bitmap? = null
    private var userId : String = ""
    private var major : Int = 0
    private var userName : String = ""
    private var selectedGrade = 1
    private val mPdfModel : PdfModel = PdfModelImpl
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handlePdfUri(uri)
            }
        }
    }

    companion object{
        private const val USERNAME = "user name"
        private const val MAJOR = "major"
        fun newIntent(context: Context,userName : String,selectedMajor : Int) : Intent{
            val intent = Intent(context,CreatePdfActivity::class.java)
            intent.putExtra(USERNAME,userName)
            intent.putExtra(MAJOR,selectedMajor)
            return  intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityCreatePdfBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        userId = sharedPreferencesHelper.getUser()?.userId.toString()
        setUpToolbar()
        major = intent.getIntExtra(MAJOR, 0)
        userName = intent.getStringExtra(USERNAME).toString()
        bindGradeSpinner()
        setUpListeners()

    }

    private fun setUpListeners(){
        mBinding.btnCancel.setOnClickListener {
            finish()
        }
        mBinding.ivUploadFile.setOnClickListener {
            selectPdfFile()
        }
        mBinding.btnCreate.setOnClickListener {
            if (isFileSelected){
                mBinding.llSaveCancel.hide()
                mBinding.progressBar.show()
                mPdfModel.updateAndUploadCoverImage(
                    bitmap = mBitmap!!,
                    onSuccess = {
                        mPdfModel.uploadPdfFile(
                            id = System.currentTimeMillis().toString(),
                            major = major,
                            file = mTempFile!!,
                            title = mBinding.etTitle.text.toString(),
                            grade = getSelectedGrade(),
                            fileSize = mBinding.etFileSize.text.toString(),
                            pages = mBinding.etBookPage.text.toString(),
                            coverImage = it,
                            language = mBinding.etLanguage.text.toString(),
                            uploadUser = userName,
                            uploadTime = System.currentTimeMillis().toString(),
                            userId = userId,
                            description = mBinding.etDescription.text.toString(),
                            onSuccess = {
                                mBinding.llSaveCancel.show()
                                mBinding.progressBar.hide()
                                Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_SHORT).show()
                                finish()
                            },
                            onFailure = {
                                mBinding.llSaveCancel.show()
                                mBinding.progressBar.hide()
                                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }, onFailure = {
                        mBinding.llSaveCancel.show()
                        mBinding.progressBar.hide()
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }

                )
            }else{
                Toast.makeText(this, "Please select your file", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.left_arrow)
    }

    private fun getSelectedGrade() : Int{
        return selectedGrade
    }

    //create activity listener
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bindGradeSpinner() {
        mBinding.spinnerGrade.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, selectedView: View?, position: Int, id: Long
                ) {
                    selectedGrade = position + 1
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    if (p0 != null) {
                        selectedGrade = 1
                    }
                }

            }
    }

    private fun selectPdfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        resultLauncher.launch(intent)
    }

    private fun handlePdfUri(uri: Uri) {
        val fileName = "uploaded_pdf_${System.currentTimeMillis()}.pdf"
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val tempFile = PDFUtils.saveInputStreamToFile(this, inputStream, fileName)
            if (tempFile != null) {
                mTempFile = tempFile
                extractAndDisplayCoverPhoto(tempFile)
            } else {
                Toast.makeText(this, "Failed to save PDF file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun extractAndDisplayCoverPhoto(pdfFile: File) {
        val bitmap: Bitmap? = PDFUtils.extractFirstPage(this, pdfFile)
        if (bitmap != null) {
            mBitmap = bitmap
            isFileSelected = true
            mBinding.ivUploadFile.setImageBitmap(bitmap)
        } else {
            Toast.makeText(this, "Failed to extract cover photo", Toast.LENGTH_SHORT).show()
        }
    }
}