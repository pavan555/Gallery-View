package com.example.pavan.galleryview.Utils

/*

   Copyright [Sai Pavan Kumar](https://github.com/pavan555) 2020

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

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
