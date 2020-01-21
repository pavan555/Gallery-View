package com.example.pavan.galleryview.Utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes

import androidx.core.content.ContextCompat

object Extensions {
    var toast: Toast? = null

    @ColorInt internal fun Context.color(@ColorRes resId: Int): Int {
        return ContextCompat.getColor(this,resId)
    }

    fun getDimension(context: Context, resId: Int): Int {
        return context.resources.getDimension(resId).toInt()
    }

    internal fun Activity.setLightNavBar( ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val view = window.decorView
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            window.decorView.systemUiVisibility = flags
        }

    }


    fun Context.setToast( message: String) {
        if (toast != null) {
            toast!!.cancel()
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        toast!!.show()
    }

}
