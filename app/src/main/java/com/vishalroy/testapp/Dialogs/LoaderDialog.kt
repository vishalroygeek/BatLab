package com.vishalroy.testapp.Dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import com.vishalroy.testapp.R

class LoaderDialog(context: Context) {

    private var dialog: Dialog = Dialog(context)

    init {
        //Initializing dialog
        dialog.setContentView(R.layout.dialog_loading)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //Making dialog non-cancellable
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
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
