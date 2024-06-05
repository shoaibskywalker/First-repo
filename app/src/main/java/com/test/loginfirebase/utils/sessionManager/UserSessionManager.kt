package com.test.loginfirebase.utils.sessionManager

import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager
    @Inject constructor(context: Context){
    private val prefs: SharedPreferences =
        context.getSharedPreferences("Login Firebase", Context.MODE_PRIVATE)

    private val prefEditor: SharedPreferences.Editor = prefs.edit()

    var userProfilePicture: String?
        get() = prefs.getString("profilePicture", null)
        set(profilePicture) {
            prefEditor.putString("profilePicture", profilePicture)
            prefEditor.apply()
        }

    var userEmailLogin: String?
        get() = prefs.getString("userEmailLogin", null)
        set(userEmail) {
            prefEditor.putString("userEmailLogin", userEmail)
            prefEditor.apply()
        }

    var userEmailSignup: String?
        get() = prefs.getString("userEmailSignup", null)
        set(userEmail) {
            prefEditor.putString("userEmailSignup", userEmail)
            prefEditor.apply()
        }

    var userNameSignup: String?
        get() = prefs.getString("userNameSignup", null)
        set(userEmail) {
            prefEditor.putString("userNameSignup", userEmail)
            prefEditor.apply()
        }

    fun saveString(key: String, value: String) {
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    // Method to retrieve a string value from shared preferences
    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
}