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

    fun addUnreadUser(userId: String) {
        val unreadUsers = prefs.getStringSet("KEY_UNREAD_USERS", mutableSetOf())?.toMutableSet()
        unreadUsers?.add(userId)
        prefEditor.putStringSet("KEY_UNREAD_USERS", unreadUsers).apply()
    }

    fun markUserAsRead(userId: String) {
        val unreadUsers = prefs.getStringSet("KEY_UNREAD_USERS", mutableSetOf())?.toMutableSet()
        unreadUsers?.remove(userId)
        prefEditor.putStringSet("KEY_UNREAD_USERS", unreadUsers).apply()
    }


    fun getUnreadUsers(): Set<String> {
        return prefs.getStringSet("KEY_UNREAD_USERS", mutableSetOf()) ?: mutableSetOf()
    }


    var currentUserPicture: String
        get() = prefs.getString("currentUserPicture", "") ?: ""
        set(value) {
            prefs.edit().putString("currentUserPicture", value).apply()
        }


     fun saveReceiverProfilePictureUrl(userId: String, url: String) {
        prefEditor.putString("profile_picture_url_$userId", url) // Use userId as part of the key
        prefEditor.apply() // or editor.commit()
    }

     fun getReceiverProfilePictureUrl(userId: String): String? {
        return prefs.getString("profile_picture_url_$userId", null) // Use the same key structure
    }

    private fun saveAboutText(text: String) {
        prefEditor.putString("aboutText", text)
        prefEditor.apply()
        }



    var userAbout: String?
        get() = prefs.getString("userAbout", null)
        set(userName) {
            prefEditor.putString("userAbout", userName)
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

    var notificationSenderUid: String?
        get() = prefs.getString("notificationSenderUid", null)
        set(notificationSenderUid) {
            prefEditor.putString("notificationSenderUid", notificationSenderUid)
            prefEditor.apply()
        }

    fun removeNotificationSenderUid() {
        prefEditor.remove("notificationSenderUid")
        prefEditor.apply()
    }

}