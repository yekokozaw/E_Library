package com.ktu.elibrary.network.storage

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ktu.elibrary.data.vo.PdfVo
import com.ktu.elibrary.data.vo.UserVo
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

object CloudFireStoreApiImpl : CloudFireStoreApi {

    @SuppressLint("StaticFieldLeak")
    private var database: FirebaseFirestore = Firebase.firestore
    private val storageRef = FirebaseStorage.getInstance().reference

    override fun addUser(userVo: UserVo) {

    }

    override fun getPdfList(
        major: Int,
        onSuccess: (pdfFiles: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.collection(major.toString())
            .addSnapshotListener { value, error ->
                error?.let {
                    onFailure(it.localizedMessage ?: "Check Internet Connection")
                } ?: run {
                    val pdfList : MutableList<PdfVo> = arrayListOf()
                    val documentList = value?.documents ?: arrayListOf()
                    for (document in documentList){
                        val data = document.data
                        val id = data?.get("id") as String
                        val title = data["title"] as String
                        val pages = data["pages"] as String
                        val fileSize = data["file_size"] as String
                        val language = data["language"] as String
                        val posterImage = data["poster_image"] as String
                        val fileUrl = data["file_url"] as? String ?: "file is null"
                        val uploadUser = data["upload_user"] as? String ?: "_"
                        val uploadTime = data["upload_time"] as String
                        val userId = data["user_id"] as? String ?: "null"
                        val description = data["description"] as? String ?: ""
                        val grade = data["grade"] as Long
                        val pdf = PdfVo(
                            id,
                            title,
                            grade = grade.toInt(),
                            pages = pages,
                            fileSize = fileSize,
                            language = language,
                            posterImage = posterImage,
                            fileUrl = fileUrl,
                            uploadUser = uploadUser,
                            uploadTime = uploadTime,
                            userId = userId,
                            description = description
                        )
                        pdfList.add(pdf)
                    }
                    onSuccess(pdfList)
                }
            }
    }

    override fun createBook(
        major: Int,
        pdfBook: PdfVo,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {

    }

    override fun deleteBook(
        major: Int,
        bookId: String,
        pdfFilePath: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.collection(major.toString())
            .document(bookId)
            .delete()
            .addOnSuccessListener {
                storageRef.child(pdfFilePath).delete().addOnSuccessListener {
                    onSuccess("File successfully deleted")
                }
                    .addOnFailureListener {
                        onFailure("Error deleting: $it")
                    }
            }
            .addOnFailureListener {
                onFailure("Error deleting: $it")
            }
    }

    private fun changeBitmapToUrlString(bitmap: Bitmap): Task<Uri> {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageRef.child("images/${UUID.randomUUID()}")
        val uploadTask = imageRef.putBytes(data)

        uploadTask.addOnFailureListener {
            //Log.i("FileUpload", "File uploaded failed")
        }.addOnSuccessListener {
            //Log.i("FileUpload", "File uploaded successful")
        }

        val urlTask = uploadTask.continueWithTask {
            return@continueWithTask imageRef.downloadUrl
        }
        return urlTask
    }

    override fun updateAndUploadCoverImage(
        bitmap: Bitmap,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val urlTask = changeBitmapToUrlString(bitmap)
        //url task complete မဖြစ်သေးပဲ ခေါ်ရင် not yet complete ဖြစ်ပြီး crash
        urlTask.addOnCompleteListener {
            onSuccess(it.result.toString())
        }.addOnFailureListener {
            onFailure(it.message.toString())
        }
    }

    override fun getSpecificUser(
        userId: String,
        onSuccess: (users: UserVo) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.collection("users")
            .document(userId)
            .addSnapshotListener { value, error ->
                error?.let {
                    onFailure(it.localizedMessage ?: "Check Internet Connection")
                } ?: run {
                    //val userList: MutableList<UserVO> = arrayListOf()
                    val data = value?.data
                    val id = data?.get("id") as String
                    val name = data["name"] as String
                    val phoneNumber = data["phone_number"] as? String ?: ""
                    val email = data["email"] as String
                    val password = data["password"] as String
                    val major = data["major"] as String
                    val imageUrl = data["image_url"] as? String ?: ""
                    val fcmKey = data["fcm_key"].toString()
                    val grade = data["grade"] as String
                    val userRole = data["user_role"] as Long
                    val user = UserVo(
                        userId = id,
                        userName = name,
                        phoneNumber= phoneNumber,
                        email= email,
                        password= password,
                        major = major,
                        imageUrl = imageUrl,
                        fcmKey = fcmKey,
                        grade = grade,
                        userRole = userRole.toInt()
                    )
                    onSuccess(user)
                }
            }
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
        userId : String,
        description : String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {

        val pdfRef = storageRef.child("pdf/$title${UUID.randomUUID()}")

        pdfRef.putFile(Uri.fromFile(file)).addOnSuccessListener {

            val urlTask = it.task.continueWithTask {
                return@continueWithTask pdfRef.downloadUrl
            }

            urlTask.addOnCompleteListener {
                val userMap = hashMapOf(
                    "id" to id,
                    "title" to title,
                    "grade" to grade,
                    "pages" to pages,
                    "file_size" to fileSize,
                    "language" to language,
                    "poster_image" to coverImage,
                    "file_url" to urlTask.result.toString(),
                    "upload_user" to uploadUser,
                    "upload_time" to uploadTime,
                    "user_id" to userId,
                    "description" to description
                )
                database.collection(major.toString())
                    .document(id)
                    .set(userMap)
                    .addOnSuccessListener {
                        onSuccess("$it")
                    }
                    .addOnFailureListener {
                        onFailure("Sorry,${it.localizedMessage}")
                    }
            }
        }.addOnFailureListener {
            onFailure(it.localizedMessage ?: "Pdf Upload Fail")
            }
    }

    override fun searchBook(
        major: Int,
        title: String,
        onSuccess: (books: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.collection(major.toString())
            .orderBy("title")
            .startAt(title)
            .endAt(title + "\uf8ff")
            .addSnapshotListener{ value, error ->
                error?.let {
                    onFailure(it.localizedMessage ?: "Check Internet Connection")
                } ?: run {
                    val pdfList : MutableList<PdfVo> = arrayListOf()
                    val documentList = value?.documents ?: arrayListOf()
                    for (document in documentList){
                        val data = document.data
                        val id = data?.get("id") as String
                        val bookTitle = data["title"] as String
                        val pages = data["pages"] as String
                        val grade = data["grade"] as Long
                        val fileSize = data["file_size"] as? String ?: ""
                        val language = data["language"] as? String ?: ""
                        val posterImage = data["poster_image"] as String
                        val fileUrl = data["file_url"] as String
                        val uploadUser = data["upload_user"] as? String ?: "user name is null"
                        val uploadTime = data["upload_time"] as? String ?: "null"

                        val pdf = PdfVo(
                            id,
                            bookTitle,
                            grade = grade.toInt(),
                            pages = pages,
                            fileSize = fileSize,
                            language = language,
                            posterImage = posterImage,
                            fileUrl = fileUrl,
                            uploadUser = uploadUser,
                            uploadTime = uploadTime
                        )
                        pdfList.add(pdf)
                    }
                    onSuccess(pdfList)
                }
            }
    }

    override fun filterBooks(
        major: Int,
        grade: Int,
        onSuccess: (books: List<PdfVo>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        database.collection(major.toString())
            .whereEqualTo("grade",grade.toLong())
            .addSnapshotListener{ value, error ->
                error?.let {
                    onFailure(it.localizedMessage ?: "Check Internet Connection")
                } ?: run {
                    val pdfList : MutableList<PdfVo> = arrayListOf()
                    val documentList = value?.documents ?: arrayListOf()
                    for (document in documentList){
                        val data = document.data
                        val id = data?.get("id") as String
                        val title = data["title"] as String
                        val grade = data["grade"] as Long
                        val pages = data["pages"] as String
                        val fileSize = data["file_size"] as String
                        val language = data["language"] as String
                        val posterImage = data["poster_image"] as String
                        val fileUrl = data["file_url"] as String
                        val uploadUser = data["upload_user"] as String
                        val uploadTime = data["upload_time"] as String

                        val pdf = PdfVo(
                            id,
                            title,
                            grade = grade.toInt(),
                            pages = pages,
                            fileSize = fileSize,
                            language = language,
                            posterImage = posterImage,
                            fileUrl = fileUrl,
                            uploadUser = uploadUser,
                            uploadTime = uploadTime
                        )
                        pdfList.add(pdf)
                    }
                    onSuccess(pdfList)
                }
            }
    }
}