package com.vishalroy.testapp.Helpers

import android.content.Context
import android.graphics.Typeface

class Typefaces(private val context: Context) {

    fun ralewayBold() : Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/Raleway-Bold.ttf")
    }

    fun dosisMedium() : Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/Dosis-Medium.ttf")
    }

    fun dosisSemiBold() : Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/Dosis-SemiBold.ttf")
    }

    fun dosisBold() : Typeface {
        return Typeface.createFromAsset(context.assets, "fonts/Dosis-Bold.ttf")
    }
}