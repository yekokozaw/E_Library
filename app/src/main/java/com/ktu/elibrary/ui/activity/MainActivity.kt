package com.ktu.elibrary.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.ktu.elibrary.R
import com.ktu.elibrary.databinding.ActivityMainBinding
import com.ktu.elibrary.extensions.SharedPreferencesHelper

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var mBinding : ActivityMainBinding
    companion object{
        fun newIntent(context : Context) : Intent {
            return Intent(context,MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        sharedPreferencesHelper = SharedPreferencesHelper(this)
        setUpToolbar()
        setUpListeners()
        drawerOpenClose()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.menu)
    }

    private fun setUpListeners(){
        mBinding.rlCivil.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,1,"Civil Engineering"))
        }
        mBinding.rlMechanical.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,2,"Mechanical Engineering"))
        }
        mBinding.rlep.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,3,"Electrical Power Engineering"))
        }
        mBinding.rlEc.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,4,"Electronics Engineering"))
        }
        mBinding.rlIt.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,5,"Information Technology"))
        }
        mBinding.rlMetal.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,6,"Metallurgy Engineering"))
        }
        mBinding.rlMechtronics.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,7,"Mechatronics Engineering"))
        }
        mBinding.rlNuclear.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,8,"Nuclear Technology"))
        }
        mBinding.rlBiotech.setOnClickListener {
            startActivity(MajorDetailsActivity.newIntent(this,9,"BioTechnology"))
        }
    }

    private fun drawerOpenClose(){
        val toggle = ActionBarDrawerToggle(
            this,
            mBinding.drawerLayout,
            mBinding.toolBar,
            R.string.DrawerOpen,
            R.string.DrawerClose
        )
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this,R.color.white)
        toggle.syncState()
        mBinding.navView.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.navProfile -> {
                startActivity(ProfileActivity.newIntent(this))
            }
            R.id.navAbout -> {
                val dialogFragment = AboutDialog.newInstance()
                dialogFragment.show(supportFragmentManager,"tag")
            }
            R.id.navLogout -> {
                onTapLogout()
            }
        }
        return true
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun onTapLogout(){
        val dialogBuilder = MaterialAlertDialogBuilder(this,R.style.RoundedAlertDialog)
            .setTitle("Logout")
            .setMessage("Are you sure to Logout?")
            .setPositiveButton("OK"){dialog,_ ->
                FirebaseAuth.getInstance().signOut()
                sharedPreferencesHelper.clearUser()
                navigateToLogin()
            }
            .setNegativeButton("Cancel"){dialog,_->
                dialog.dismiss()
            }
        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}