package com.ktu.elibrary.data.model

import android.graphics.Bitmap
import com.ktu.elibrary.data.vo.UserVo
import com.ktu.elibrary.network.auth.AuthManager
import com.ktu.elibrary.network.auth.FirebaseAuthManager

object AuthModelImpl : AuthModel {
    override var mAuthManager: AuthManager = FirebaseAuthManager
    override fun login(
        email: String,
        password: String,
        onSuccess: (userId : String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mAuthManager.login(email, password, onSuccess, onFailure)
    }

    override fun register(
        userName: String,
        phoneNumber: String,
        email: String,
        password: String,
        major: Int,
        imageUrl: String,
        fcmKey: String,
        grade: Int,
        userRole : Int,
        onSuccess: (user: UserVo) -> Unit,
        onFailure: (String) -> Unit
    ) {
        var majorName = ""
        majorName = when(major){
            1 -> "Civil"
            2 -> "Mechanical"
            3 -> "EP"
            4 -> "EC"
            5 -> "IT"
            6 -> "Metal"
            7 -> "Mechatronic"
            8 -> "Nuclear"
            9 -> "Biotech"
            else -> "Other"
        }
        var gradeName = ""
        gradeName = when(grade){
            1 -> "First Year"
            2 -> "Second Year"
            3 -> "Third Year"
            4 -> "Fourth Year"
            5 -> "Fifth Year"
            6 -> "Sixth Year"
            else -> "Other "
        }
        mAuthManager.register(
            userName = userName,
            phoneNumber = phoneNumber,
            email= email,
            password = password,
            major = majorName,
            imageUrl = imageUrl,
            fcmKey = fcmKey,
            grade = gradeName,
            userRole = userRole,
            onSuccess,
            onFailure
        )
    }

    override fun getCurrentUser() : String{
        return mAuthManager.getCurrentUserId()
    }

    override fun updateAndUploadProfileImage(
        bitmap: Bitmap,
        user: UserVo,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit)
    {
        mAuthManager.updateAndUploadProfileImage(bitmap,user,onSuccess,onFailure)
    }

    override fun sendPasswordResetEmail(
        email: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        mAuthManager.sendPasswordResetEmail(email,onSuccess,onFailure)
    }
}