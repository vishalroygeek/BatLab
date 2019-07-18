package com.vishalroy.testapp.Dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.vishalroy.testapp.Helpers.Typefaces
import com.vishalroy.testapp.Helpers.Utils
import com.vishalroy.testapp.MainActivity
import com.vishalroy.testapp.Models.Visitor
import com.vishalroy.testapp.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.title_text
import kotlinx.android.synthetic.main.dialog_welcome.*

class WelcomeDialog (context: Context, val visitor: Visitor, mainActivity: MainActivity){

    var dialog: Dialog = Dialog(context)
    private val typefaces = Typefaces(context)

    init {
        //Initializing dialog
        dialog.setContentView(R.layout.dialog_welcome)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //Making dialog non-cancellable
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        //Changing typefaces
        dialog.title_text.typeface = typefaces.ralewayBold()
        dialog.message_text.typeface = typefaces.dosisSemiBold()
        dialog.thanks_btn.typeface = typefaces.dosisBold()

        //Loading visitor's image in the image view
        loadUserImage()

        //Converting visitor's visit count in ordinal form
        var visit_count = Utils().convertToOrdinal(visitor.visit_count)

        //Greeting visitor with number in message
        dialog.message_text.text =  "${context.getString(R.string.hey)}, ${visitor.number} ! ${context.getString(R.string.glad_to_see)} $visit_count ${context.getString(R.string.time_batlab)}"

        dialog.thanks_btn.setOnClickListener {
            //Dismissing dialog & resetting activity
            //when thanks button is clicked
            dismiss()
            mainActivity.resetActivity()
        }
    }

    private fun loadUserImage(){
        Picasso.get()
            .load(visitor.image)
            .placeholder(R.drawable.app_logo)
            .error(R.drawable.app_logo)
            .into(dialog.visitor_image)
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }

}