package com.vishalroy.testapp

import android.app.Activity
import android.graphics.Typeface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.mukesh.OnOtpCompletionListener
import com.vishalroy.testapp.Helpers.Typefaces
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val typefaces = Typefaces(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //Changing typefaces
        title_text.typeface = typefaces.ralewayBold()
        description.typeface = typefaces.dosisMedium()
        timer.typeface = typefaces.dosisMedium()
        user_number.typeface = typefaces.dosisSemiBold()
        otp_view.typeface = typefaces.dosisSemiBold()
        submit_btn.typeface = typefaces.dosisBold()

    }

}
