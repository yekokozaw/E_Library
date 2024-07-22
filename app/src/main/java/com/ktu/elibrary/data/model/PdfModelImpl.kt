package com.ktu.elibrary.data.model

import android.graphics.Bitmap
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.data.vo.UserVo
import com.ktu.elibrary.network.storage.CloudFireStoreApi
import com.ktu.elibrary.network.storage.CloudFireStoreApiImpl
import java.io.File

object PdfModelImpl : PdfModel {
    override var mFirebaseApi: CloudFireStoreApi = CloudFireStoreApiImpl
    override fun getPdfList(
        major: Int,
        onSuccess: (pdfFiles: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.getPdfList(major,onSuccess,onFailure)
    }

    override fun createBook(
        major: Int,
        pdfBook: PdfVo,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.createBook(
            major = major,
            pdfBook = pdfBook,
            onSuccess,
            onFailure
        )
    }

    override fun deleteBook(
        major: Int,
        bookId: String,
        pdfFilePath: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.deleteBook(
            major,
            bookId,
            pdfFilePath,
            onSuccess,
            onFailure
        )
    }

    override fun updateAndUploadCoverImage(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.updateAndUploadCoverImage(bitmap,onSuccess,onFailure)
    }

    override fun getSpecificUser(
        userId: String,
        onSuccess: (users: UserVo) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.getSpecificUser(
            userId = userId,
            onSuccess,
            onFailure
        )
    }

    override fun uploadPdfFile(
        id : String,
        major: Int,
        file: File,
        title: String,
        grade: Int,
        fileSize: String,
        pages: String,
        language: String,
        coverImage: String,
        uploadUser: String,
        uploadTime: String,
        userId: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.uploadPdfFile(
            id = id,
            major,
            file,
            title,
            grade,
            fileSize,
            pages,
            language,
            coverImage,
            uploadUser,
            uploadTime,
            userId,
            onSuccess,
            onFailure
        )
    }

    override fun searchBook(
        major: Int,
        title: String,
        onSuccess: (books: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.searchBook(
            major = major,
            title = title,
            onSuccess,
            onFailure
        )
    }

    override fun filterBooks(
        major: Int,
        grade: Int,
        onSuccess: (books: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseApi.filterBooks(
            major = major,
            grade = grade,
            onSuccess,
            onFailure
        )
    }
}