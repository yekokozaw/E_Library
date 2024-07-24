package com.ktu.elibrary.data.vo

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PdfVo(
    val id : String = "",
    val title : String = "",
    val pages : String = "",
    val fileSize : String = "",
    val language : String = "",
    val posterImage : String = "",
    val fileUrl : String = "",
    val uploadUser : String = "",
    val uploadTime : String = "",
    val userId : String = "",
    val grade : Int = 0,
    val description : String = ""
) : Parcelable