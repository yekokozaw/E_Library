package com.ktu.elibrary.network.auth

import android.graphics.Bitmap
import com.ktu.elibrary.data.vo.UserVo

interface AuthManager {

    fun login(email: String, password: String, onSuccess: (userId : String) -> Unit, onFailure: (String) -> Unit)

    fun register(
        userName:String,
        phoneNumber:String,
        email: String,
        password: String,
        major:String,
        imageUrl:String,
        fcmKey:String,
        grade : String,
        userRole : Int,
        onSuccess: (user: UserVo) -> Unit,
        onFailure: (String) -> Unit
    )

    fun getCurrentUserId() : String

    fun updateAndUploadProfileImage(bitmap: Bitmap, user: UserVo,onSuccess: () -> Unit,onFailure: (String) -> Unit)

    fun sendPasswordResetEmail(email : String,onSuccess: (String) -> Unit,onFailure: (String) -> Unit)
}