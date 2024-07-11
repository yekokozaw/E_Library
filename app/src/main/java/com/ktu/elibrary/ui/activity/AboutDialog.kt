package com.ktu.elibrary.ui.activity

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.ktu.elibrary.R
import com.ktu.elibrary.databinding.DialogAboutBinding

class AboutDialog : DialogFragment() {

    private lateinit var mBinding : DialogAboutBinding
    companion object{
        fun newInstance() : AboutDialog{
            val fragment = AboutDialog()
            return  fragment

        }
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawableResource(R.drawable.rounded_dialog)
        dialog?.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DialogAboutBinding.inflate(layoutInflater)
        return mBinding.root


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding.btnGraduateCancel.setOnClickListener {
            dismiss()
        }
    }
}