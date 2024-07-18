package com.ktu.elibrary.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.AuthModel
import com.ktu.elibrary.data.model.AuthModelImpl
import com.ktu.elibrary.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityResetPasswordBinding
    private val mAuthModel : AuthModel = AuthModelImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setUpListeners()
    }

    private fun setUpListeners(){
        mBinding.btnResetPassword.setOnClickListener {
            if (mBinding.etResetEmail.text.toString().isNotEmpty()){
                mAuthModel.sendPasswordResetEmail(
                    mBinding.etResetEmail.text.toString(),
                    onSuccess = {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    },
                    onFailure = {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else{
                mBinding.etResetEmail.error = "Please enter your email"

            }
        }
    }

}