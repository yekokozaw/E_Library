package com.ktu.elibrary.network.auth

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.ktu.elibrary.data.vo.UserVo
import java.io.ByteArrayOutputStream
import java.util.UUID

object FirebaseAuthManager : AuthManager {

    private var database = Firebase.firestore
    private val mFirebaseAuth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference
    override fun login(
        email: String,
        password: String,
        onSuccess: (userId : String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful && it.isComplete) {
                val user = mFirebaseAuth.currentUser
                if (user?.isEmailVerified == true){
                    onSuccess(user.uid)
                }else{
                    onFailure("Email Verification is required")
                }
            } else {
                onFailure(it.exception?.message ?: "Check Internet Connection")
            }
        }
    }

    override fun register(
        userName: String,
        phoneNumber: String,
        email: String,
        password: String,
        major: String,
        imageUrl: String,
        fcmKey: String,
        grade: String,
        userRole: Int,
        onSuccess: (user: UserVo) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful && it.isComplete) {
                val user = mFirebaseAuth.currentUser
                user?.sendEmailVerification()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            mFirebaseAuth.currentUser?.updateProfile(
                                UserProfileChangeRequest.Builder().setDisplayName(userName).build()
                            )
                            onSuccess(UserVo(
                                getCurrentUserId(),userName,phoneNumber,email,
                                password, major,imageUrl,fcmKey,grade,userRole))
                        }else{
                            onFailure(task.exception?.message ?: "Something wrong")
                        }
                    }

            } else {
                onFailure(it.exception?.message ?: "Check Internet Connection")
            }
        }
    }

    override fun getCurrentUserId(): String {
        return mFirebaseAuth.currentUser?.uid.toString()
    }

    private fun changeBitmapToUrlString(bitmap: Bitmap): Task<Uri> {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val imageRef = storageRef.child("profiles/${UUID.randomUUID()}")
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

    override fun updateAndUploadProfileImage(
        bitmap: Bitmap,
        user: UserVo,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    )
    {
        val urlTask = changeBitmapToUrlString(bitmap)

        urlTask.addOnCompleteListener { it ->
            val imageUrl = it.result?.toString()
            val userMap = hashMapOf(
                "id" to user.userId,
                "name" to user.userName,
                "phone_number" to user.phoneNumber,
                "email" to user.email,
                "password" to user.password,
                "major" to user.major,
                "image_url" to imageUrl,
                "fcm_key" to user.fcmKey,
                "grade" to user.grade,
                "user_role" to user.userRole
            )

            database.collection("users")
                .document(user.userId)
                .set(userMap)
                .addOnSuccessListener {
                    onSuccess()
                }.addOnFailureListener {error ->
                    onFailure(error.toString())
                }
        }
    }

}

