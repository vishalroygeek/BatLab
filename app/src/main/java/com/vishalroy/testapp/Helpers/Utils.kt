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

}