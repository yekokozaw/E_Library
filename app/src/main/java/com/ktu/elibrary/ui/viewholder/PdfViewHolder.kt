package com.ktu.elibrary.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.databinding.ViewholderPdfListBinding
import com.ktu.elibrary.delegates.pdfViewHolderDelegate

class PdfViewHolder(private val binding: ViewholderPdfListBinding, private val mDelegate : pdfViewHolderDelegate)
    :RecyclerView.ViewHolder(binding.root){

    private var mUser : PdfVo? = null
    init {
        binding.root.setOnClickListener {
            mUser?.let { it1 -> mDelegate.onTapPdfViewHolder(it1) }
            true
        }
    }

    fun bindData(pdf : PdfVo){
        mUser = pdf
        binding.tvTitle.text = pdf.title
        binding.tvGradeName.text = getGradeName(pdf.grade)

        Glide.with(binding.root.context)
            .load(pdf.posterImage)
            .into(binding.ivPosterImage)
    }

    private fun getGradeName(grade : Int) : String{
        var name = ""
        name = when(grade){
            1 -> "First Year"
            2 -> "Second Year"
            3 -> "Third Year"
            4 -> "Fourth Year"
            5 -> "Fifth Year"
            6 -> "Final Year"
            else -> "_"
        }
        return  name
    }
}