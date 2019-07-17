package com.vishalroy.testapp.Dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Rational
import android.util.Size
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.vishalroy.testapp.BuildConfig
import com.vishalroy.testapp.R
import kotlinx.android.synthetic.main.dialog_camera.*
import java.io.File

class CameraDialog (activity: Activity){

    private var dialog: Dialog = Dialog(activity)

    init {
        //Initializing dialog
        dialog.setContentView(R.layout.dialog_camera)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //Making dialog non-cancellable
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)


        //Setting up preview configuration
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(3, 4))
            setTargetResolution(Size(540, 960))
        }.build()

        //Setting up output configurations
        val imageCaptureConfig = ImageCaptureConfig.Builder()
            .apply {
                setTargetAspectRatio(Rational(3, 4))
                setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                setTargetResolution(Size(1080, 1920))
            }.build()


        val preview = Preview(previewConfig)
        val imageCapture = ImageCapture(imageCaptureConfig)

        dialog.view_finder.setOnClickListener {

            val file = File(activity.getExternalFilesDir(BuildConfig.APPLICATION_ID), "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(error: ImageCapture.UseCaseError,
                                         message: String, exc: Throwable?) {

                    }

                    override fun onImageSaved(file: File) {

                    }
                })
        }


        CameraX.bindToLifecycle(this as LifecycleOwner, preview, imageCapture)
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