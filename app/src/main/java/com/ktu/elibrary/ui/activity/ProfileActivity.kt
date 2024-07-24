package com.ktu.elibrary.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.PdfModel
import com.ktu.elibrary.data.model.PdfModelImpl
import com.ktu.elibrary.data.vo.UserVo
import com.ktu.elibrary.databinding.ActivityProfileBinding
import com.ktu.elibrary.extensions.SharedPreferencesHelper

class ProfileActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityProfileBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private val pdfModel : PdfModel = PdfModelImpl

    companion object{
        fun newIntent(context: Context) : Intent {
            return Intent(context,ProfileActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setUpToolbar()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val user = sharedPreferencesHelper.getUser()
        if (user != null) {
            setUpNetworkCall(user.userId)
        }else{
            Toast.makeText(this, "user id is null", Toast.LENGTH_SHORT).show()
        }
        setUpListeners()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.left_arrow)
    }

    private fun setUpListeners(){
        mBinding.ivCopyImage.setOnClickListener {
            val copyText = mBinding.tvUserId.text.toString()
            copyToClipboard(copyText)
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()
        }
    }
    private fun setUpNetworkCall(userId : String){
        pdfModel.getSpecificUser(
            userId,
            onSuccess = {
                bindUserData(it)
            },
            onFailure = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )
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

    private fun bindUserData(user : UserVo){
        mBinding.tvUserName.text = user.userName
        mBinding.tvEmail.text = user.email
        mBinding.tvUserId.text = user.userId
        mBinding.tvPhoneNumber.text = user.phoneNumber
        mBinding.tvMajorName.text = user.major
        Glide.with(this)
            .load(user.imageUrl)
            .placeholder(R.drawable.profile)
            .into(mBinding.ivUserImage)
    }

    private fun copyToClipboard(text: String) {
        val clipboardManager = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", text)
        clipboardManager.setPrimaryClip(clipData)
    }
}