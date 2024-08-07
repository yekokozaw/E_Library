package com.ktu.elibrary.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.AuthModel
import com.ktu.elibrary.data.model.AuthModelImpl
import com.ktu.elibrary.data.model.PdfModel
import com.ktu.elibrary.data.model.PdfModelImpl
import com.ktu.elibrary.databinding.ActivityLoginBinding
import com.ktu.elibrary.extensions.SharedPreferencesHelper
import com.ktu.elibrary.extensions.hide
import com.ktu.elibrary.extensions.show

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var mBinding: ActivityLoginBinding
    private val pdfModel : PdfModel = PdfModelImpl
    private val mAuthModel : AuthModel = AuthModelImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        setUpListeners()
    }

    private fun setUpListeners(){
        mBinding.tvForgetPassword.setOnClickListener {
            val intent = Intent(this,ResetPasswordActivity::class.java)
            startActivity(intent)
        }
        mBinding.btnLogin.setOnClickListener {
            val email = mBinding.etEmail.text.toString()
            val password = mBinding.etPassword.text.toString()
            if (email.isNotEmpty() && password.isNotEmpty()){
                mBinding.progressBar.show()
                mBinding.btnLogin.alpha = 0.5f
                mBinding.btnLogin.isEnabled = false
                mAuthModel.login(
                    email,
                    password,
                    onSuccess = {
                        pdfModel.getSpecificUser(
                            it,
                            onSuccess = { user ->
                                sharedPreferencesHelper.saveUser(user.userName,user.userId,user.email,user.userRole)
                                navigateToMain()
                                finish()
                            }, onFailure = {

                            }
                        )
                    },
                    onFailure = {
                        Toast.makeText(this,it,Toast.LENGTH_SHORT).show()
                        mBinding.progressBar.hide()
                        mBinding.btnLogin.alpha = 1.0f
                        mBinding.btnLogin.isEnabled = true
                    })
            }else if (email.isEmpty()){
                mBinding.etEmail.error = "Please enter email"
            }
            else{
                mBinding.etPassword.error = "Please enter password"
            }
        }

        mBinding.tvRegister.setOnClickListener {
            val intent = Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the current activity
    }


}