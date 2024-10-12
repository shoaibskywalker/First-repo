package com.test.loginfirebase.utils.sessionManager

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSessionManager
    @Inject constructor(context: Context){
    private val prefs: SharedPreferences =
        context.getSharedPreferences("Login Firebase", Context.MODE_PRIVATE)

    private val prefEditor: SharedPreferences.Editor = prefs.edit()

    // Method to save the image as a Base64 string in SharedPreferences
    fun saveUserProfileImage(bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
        prefEditor.putString("profilePicture", encodedImage)
        prefEditor.apply()
    }


    // Method to retrieve the image as a Bitmap from SharedPreferences
    fun getUserProfileImage(): Bitmap? {
        val encodedImage = prefs.getString("profilePicture", null) ?: return null
        val byteArray = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

    fun saveUserProfileImage(userEmail: String, bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageBytes = baos.toByteArray()
        val imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT)
        prefEditor.putString("${userEmail}_profile_image", imageString)
        prefEditor.apply()
    }

    fun getUserProfileImage(userEmail: String): Bitmap? {
        val imageString = prefs.getString("${userEmail}_profile_image", null)
        return imageString?.let {
            val imageBytes = Base64.decode(it, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    }

    // Clear the saved image when user logs out
    fun clearUserProfileImage(userEmail: String) {
        prefEditor.remove("${userEmail}_profile_image")
        prefEditor.apply()
    }

    var userEmailLogin: String
        get() = prefs.getString("userEmailLogin", "") ?: ""
        set(value) {
            prefs.edit().putString("userEmailLogin", value).apply()
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

    var userNameLogin: String?
        get() = prefs.getString("userNameLogin", null)
        set(userName) {
            prefEditor.putString("userNameLogin", userName)
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