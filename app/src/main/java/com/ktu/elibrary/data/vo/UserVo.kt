package com.ktu.elibrary.data.vo

data class UserVo(
    val userId:String = "",
    val userName:String = "",
    val phoneNumber:String = "",
    val email: String = "",
    val password: String = "",
    val major:String = "",
    var imageUrl:String = "",
    var fcmKey:String = "",
    var grade: String = "",
    var userRole : Int = 0
)
