package com.test.loginfirebase.utils

import android.content.Context
import android.widget.Toast

object CommonUtil {

     fun showToastMessage(context: Context,message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}