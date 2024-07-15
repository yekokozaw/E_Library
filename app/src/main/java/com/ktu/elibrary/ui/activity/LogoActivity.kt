package com.ktu.elibrary.ui.activity

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ktu.elibrary.data.model.PdfModel
import com.ktu.elibrary.data.model.PdfModelImpl
import com.ktu.elibrary.databinding.ActivityLogoBinding
import com.ktu.elibrary.extensions.SharedPreferencesHelper

class LogoActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var mBinding : ActivityLogoBinding
    private lateinit var auth: FirebaseAuth
    private val pdfModel : PdfModel = PdfModelImpl
    private val handler = Handler(Looper.getMainLooper())
    private var loadingDots = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLogoBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        auth = FirebaseAuth.getInstance()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val currentUser = auth.currentUser
        startLoadingAnimation()
        Handler(Looper.getMainLooper()).postDelayed({
            if (currentUser != null){
                pdfModel.getSpecificUser(
                    currentUser.uid,
                    onSuccess = {
                        sharedPreferencesHelper.saveUser(it.userName,it.userId,it.email,it.userRole)
                        startActivity(MainActivity.newIntent(context = this))
                        finish()
                    }, onFailure = {
                        Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                    }
                )
            }
            else{
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        },2000)

    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun startLoadingAnimation() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                val dots = when (loadingDots) {
                    0 -> ""
                    1 -> "."
                    2 -> ".."
                    else -> "..."
                }
                mBinding.tvWelcome.text = "Loading$dots"
                loadingDots = (loadingDots + 1) % 4
                handler.postDelayed(this, 500)
            }
        }, 500)
    }
}