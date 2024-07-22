package com.ktu.elibrary.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.ktu.elibrary.R
import com.ktu.elibrary.data.model.PdfModel
import com.ktu.elibrary.data.model.PdfModelImpl
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.databinding.ActivityMajorDetailsBinding
import com.ktu.elibrary.delegates.pdfViewHolderDelegate
import com.ktu.elibrary.extensions.SharedPreferencesHelper
import com.ktu.elibrary.extensions.hide
import com.ktu.elibrary.extensions.show
import com.ktu.elibrary.ui.adapter.PdfListAdapter

class MajorDetailsActivity : AppCompatActivity() ,pdfViewHolderDelegate{

    private lateinit var mBinding : ActivityMajorDetailsBinding
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private var userRole = 0
    private var userName = ""
    private lateinit var mPdfAdapter : PdfListAdapter
    private var major : Int = 0
    private var majorName : String = ""
    private val mPdfModel : PdfModel = PdfModelImpl
    companion object{
        private val MAJOR = "major"
        private const val NAME = "name"
        fun newIntent(context : Context,major : Int,majorName : String) : Intent {
            val intent = Intent(context,MajorDetailsActivity::class.java)
            intent.putExtra(MAJOR,major)
            intent.putExtra(NAME,majorName)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMajorDetailsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        setUpToolbar()
        major = intent.getIntExtra(MAJOR,0)
        majorName = intent.getStringExtra(NAME).toString()
        mBinding.tvTitle.text = majorName
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val user = sharedPreferencesHelper.getUser()
        userName = user?.userName ?: "anonymous"
        userRole = user?.userRole!!
        setUpNetworkCall()
        setUpRecyclerView()
        setUpListeners()
        bindYearSpinner()
    }

    private fun setUpToolbar() {
        setSupportActionBar(mBinding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.left_arrow)
    }

    //create icon
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add,menu)

        val itemId = R.id.action_add
        if (userRole != 2){
            menu?.findItem(itemId)?.isVisible = false
        }
        return true
    }

    //create activity listener
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_add -> {
                startActivity(CreatePdfActivity.newIntent(this, userName = userName, selectedMajor = major))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpListeners(){
        mBinding.tlSearch.setEndIconOnClickListener {
            val title = mBinding.etSearch.text.toString()
            mPdfModel.searchBook(
                major = major,
                title = title,
                onSuccess = {
                            if (it.isEmpty()){
                                mBinding.rvPdfBook.hide()
                            }else{
                                mBinding.rvPdfBook.show()
                                mPdfAdapter.setNewData(it)
                            }
                }, onFailure = {
                    Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun setUpNetworkCall(){
        mPdfModel.getPdfList(
            major,
            onSuccess = {
                mPdfAdapter.setNewData(it)
            },
            onFailure = {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun setUpRecyclerView(){
        mPdfAdapter = PdfListAdapter(this)
        mBinding.rvPdfBook.adapter = mPdfAdapter
        mBinding.rvPdfBook.layoutManager = GridLayoutManager(this,2)
    }

    override fun onTapPdfViewHolder(pdf : PdfVo) {
        startActivity(BookDetailsActivity.newIntent(this,pdf,userRole,major))
    }

    private fun bindYearSpinner() {
        mBinding.spinnerYear.setSelection(0)
        mBinding.spinnerYear.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, selectedView: View?, position: Int, id: Long
                ) {
                    if (position == 0){
                        setUpNetworkCall()
                    }else{
                        mPdfModel.filterBooks(
                            major = major,
                            grade = position,
                            onSuccess = {
                                mPdfAdapter.setNewData(it)
                            },
                            onFailure = {
                                Toast.makeText(this@MajorDetailsActivity, it, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
    }
}