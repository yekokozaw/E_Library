package com.ktu.elibrary.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.databinding.ViewholderPdfListBinding
import com.ktu.elibrary.delegates.pdfViewHolderDelegate
import com.ktu.elibrary.ui.viewholder.PdfViewHolder

class PdfListAdapter(private val delegate : pdfViewHolderDelegate)
    :RecyclerView.Adapter<PdfViewHolder>(){
    private var mPdfs : List<PdfVo> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding = ViewholderPdfListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PdfViewHolder(binding,delegate)
    }

    override fun getItemCount(): Int {
        return mPdfs.size
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.bindData(mPdfs[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setNewData(pdfs : List<PdfVo>){
        mPdfs = pdfs
        notifyDataSetChanged()
    }
}