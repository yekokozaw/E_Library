package com.ktu.elibrary.data.model

import android.graphics.Bitmap
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.data.vo.UserVo
import com.ktu.elibrary.network.storage.CloudFireStoreApi
import java.io.File

interface PdfModel {

    var mFirebaseApi: CloudFireStoreApi

    fun getPdfList(
        major : Int,
        onSuccess : (pdfFiles : List<PdfVo>)->Unit,
        onFailure: (String) -> Unit
    )

    fun createBook(
        major: Int,
        pdfBook : PdfVo,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun deleteBook(
        major: Int,
        bookId : String,
        pdfFilePath : String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun updateAndUploadCoverImage(bitmap: Bitmap, onSuccess: (String) -> Unit, onFailure: (String) -> Unit)

    fun getSpecificUser(
        userId: String,
        onSuccess: (users: UserVo) -> Unit,
        onFailure: (String) -> Unit
    )

    fun uploadPdfFile(
        id : String,
        major: Int,
        file : File,
        title : String,
        grade: Int,
        fileSize : String,
        pages : String,
        language : String,
        coverImage : String,
        uploadUser : String,
        uploadTime : String,
        userId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    )

    fun searchBook(
        major: Int,
        title : String,
        onSuccess: (books : List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    )

    fun filterBooks(
        major: Int,
        grade : Int,
        onSuccess: (books: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    )
}