package com.vishalroy.testapp.Helpers

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

class Utils {

    fun toast(context: Context, text : String){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    fun snackBar(view : View, text : String){
       Snackbar.make(view,text, Snackbar.LENGTH_SHORT).show()
    }

    fun convertToOrdinal(i: Int): String {
        val sufixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
        when (i % 100) {
            11, 12, 13 -> return i.toString() + "th"
            else -> return i.toString() + sufixes[i % 10]
        }
    }

}