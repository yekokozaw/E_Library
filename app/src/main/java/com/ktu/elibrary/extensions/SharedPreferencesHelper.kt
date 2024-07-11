package com.ktu.elibrary.extensions

import android.content.Context
import android.content.SharedPreferences
import com.ktu.elibrary.data.vo.UserVo

class SharedPreferencesHelper(context : Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val KEY_ROLE = "role"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
    }

    fun saveUser(userName : String,userId : String,email : String,role : Int) {
        with(sharedPreferences.edit()) {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, userName)
            putString(KEY_EMAIL, email)
            putInt(KEY_ROLE,role)
            apply()
        }
    }

    fun getUser(): UserVo? {
        val userId = sharedPreferences.getString(KEY_USER_ID, null)
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val email = sharedPreferences.getString(KEY_EMAIL, null)
        val role = sharedPreferences.getInt(KEY_ROLE,0)
        return if (userId != null && username != null && email != null) {
            UserVo(userId = userId, userName =  username, email = email, userRole = role)
        } else {
            null
        }
    }

    fun clearUser() {
        sharedPreferences.edit().clear().apply()
    }
}