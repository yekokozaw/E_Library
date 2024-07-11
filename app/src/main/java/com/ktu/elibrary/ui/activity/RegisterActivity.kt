package com.ktu.elibrary.ui.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.AuthModel
import com.ktu.elibrary.data.model.AuthModelImpl
import com.ktu.elibrary.databinding.ActivityRegisterBinding
import com.ktu.elibrary.extensions.hide
import com.ktu.elibrary.extensions.show
import java.io.IOException

class RegisterActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityRegisterBinding
    private val mAuthModel : AuthModel = AuthModelImpl
    private var isImageSelected = false
    private lateinit var fcmToken : String
    private lateinit var imageUri : Uri
    private var bitmap: Bitmap? = null
    private var selectedGrade = 1
    private var selectedMajor = 1

    override fun onStart() {
        super.onStart()
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            fcmToken = it.result
        }
    }

    private val requestGalleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openImageFromGallery()
        } else {
            Toast.makeText(this, "Permission error", Toast.LENGTH_SHORT).show()
        }
    }

    private val openGalleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data
            if (selectedImageUri != null) {
                imageUri = selectedImageUri
                isImageSelected = true
                mBinding.ivUploadProfile.setImageURI(imageUri)
            }
            imageDecode()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setUpListeners()
        bindMajorSpinner()
        bindGradeSpinner()
    }

    private fun setUpListeners(){
            mBinding.ivUploadProfile.setOnClickListener {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Permission already granted, open the gallery
                        openImageFromGallery()
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                            requestGalleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }else{
                            requestGalleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    }
            }

            mBinding.btnRegister.setOnClickListener {
                val userName = mBinding.etUserName.text.toString()
                val phoneNumber = mBinding.etPhoneNumber.text.toString()
                val password = mBinding.etPassword.text.toString()
                val email = mBinding.etEmail.text.toString()
                if (validateInput()){
                    mBinding.llSaveCancel.hide()
                    mBinding.registerLoadingBar.show()
                    Toast.makeText(this, "Please wait, this can take a few minute", Toast.LENGTH_SHORT).show()
                    mAuthModel.register(
                        userName,
                        phoneNumber,
                        email,
                        password,
                        major = getSelectedMajor(),
                        imageUrl = "",
                        fcmToken,
                        grade = getSelectedGrade() ,
                        userRole = 1,
                        onSuccess = {user ->
                            bitmap?.let { it1 ->
                                mAuthModel.updateAndUploadProfileImage(
                                    it1,
                                    user,
                                    onSuccess = {
                                        mBinding.llSaveCancel.show()
                                        mBinding.registerLoadingBar.hide()
                                        showSuccessDialog()
                                    },
                                    onFailure = {
                                        mBinding.llSaveCancel.show()
                                        mBinding.registerLoadingBar.hide()
                                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                                    })
                            }?: Toast.makeText(this,"Bitmap Error",Toast.LENGTH_SHORT).show()
                        },
                        onFailure = {
                            mBinding.llSaveCancel.show()
                            mBinding.registerLoadingBar.hide()
                            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

    private fun openImageFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        openGalleryLauncher.launch(galleryIntent)
    }

    private fun getSelectedMajor() : Int{
        return selectedMajor
    }

    private fun getSelectedGrade() : Int{
        return selectedGrade
    }

    private fun validateInput() : Boolean{
        return when {
            !isImageSelected -> {
                Toast.makeText(this, "Please select Image", Toast.LENGTH_SHORT).show()
                return false
            }
            mBinding.etUserName.text.toString().isEmpty() -> {
                mBinding.etUserName.error = "required"
                return false
            }
            mBinding.etPhoneNumber.text.toString().isEmpty() -> {
                mBinding.etPhoneNumber.error = "require"
                return false
            }
            mBinding.etEmail.text.toString().isEmpty() -> {
                mBinding.etEmail.error = "required"
                return false
            }
            mBinding.etPassword.text.toString().isEmpty() -> {
                mBinding.etPassword.error = "required"
                return false
            }

            else -> true
        }
    }

    private fun bindMajorSpinner() {
        mBinding.spinnerMajor.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, selectedView: View?, position: Int, id: Long
                ) {
                    selectedMajor = position + 1
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    if (p0 != null) {
                        selectedMajor = 4
                    }
                }

            }
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

    private fun imageDecode(){
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                val source = ImageDecoder.createSource(this.contentResolver, imageUri)
                val bitmapImage = ImageDecoder.decodeBitmap(source)
                bitmap = bitmapImage
            } else {
                val bitmapImage = MediaStore.Images.Media.getBitmap(
                    applicationContext.contentResolver, imageUri
                )
                bitmap = bitmapImage
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showSuccessDialog(){
        val builder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        builder.setTitle("Registration Success")
            .setMessage("Check you email.You can log in if the verification success.")
            .setPositiveButton("OK"){dialog,_ ->
                dialog.dismiss()
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
        val dialog = builder.create()
        dialog.show()
    }
}