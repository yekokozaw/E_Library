package com.ktu.elibrary.data.model

import android.graphics.Bitmap
import com.ktu.elibrary.data.vo.UserVo
import com.ktu.elibrary.network.auth.AuthManager

interface AuthModel {
    var mAuthManager : AuthManager

    fun login(email: String, password: String, onSuccess: (userId : String) -> Unit, onFailure: (String) -> Unit)

    fun register(
        userName:String,
        phoneNumber:String,
        email: String,
        password: String,
        major : Int,
        imageUrl:String,
        fcmKey:String,
        grade : Int,
        userRole : Int,
        onSuccess: (user: UserVo) -> Unit,
        onFailure: (String) -> Unit
    )

    fun getCurrentUser() : String

    fun updateAndUploadProfileImage(bitmap: Bitmap, user: UserVo,onSuccess: () -> Unit,onFailure: (String) -> Unit)
}