package com.vishalroy.testapp.Dialogs

import android.app.Activity
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.lifecycle.LifecycleOwner
import com.vishalroy.testapp.Helpers.Constants
import com.vishalroy.testapp.Helpers.Utils
import com.vishalroy.testapp.R
import id.zelory.compressor.Compressor
import kotlinx.android.synthetic.main.dialog_camera.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class CameraDialog (var activity: Activity){

    private var dialog : Dialog = Dialog(activity)
    private var onImageCaptured: OnImageCaptured? = null
    private lateinit var soundPool : SoundPool
    private var shutterSound : Int = 0

    init {
        //Initializing dialog
        dialog.setContentView(R.layout.dialog_camera)
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(false)

        //Initializing soundPool
        initSoundPool()

        dialog.texture.post {
            startCamera()
        }

        // Every time the provided texture view changes, recompute layout
        dialog.texture.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        //Just for fun ;)
        Handler().postDelayed({
            Utils().snackBar(dialog.root_view, activity.getString(R.string.say_cheese))
        }, 1000)

    }

    private fun initSoundPool(){
        //Configuring attributes
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        //Initializing
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        //Loading shutter sound to soundPool
        shutterSound = soundPool.load(activity, R.raw.camera_shutter, 1)
    }

    private fun startCamera() {
        val metrics = DisplayMetrics().also { dialog.texture.display.getRealMetrics(it) }
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)
        val screenAspectRatio = Rational(metrics.widthPixels, metrics.heightPixels)

        //Setting up the preview configuration
        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(CameraX.LensFacing.FRONT)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenAspectRatio)
            setTargetRotation(activity.windowManager.defaultDisplay.rotation)
            setTargetRotation(dialog.texture.display.rotation)
        }.build()

        val preview = Preview(previewConfig)
        preview.setOnPreviewOutputUpdateListener {
            dialog.texture.surfaceTexture = it.surfaceTexture
            updateTransform()
        }


        //Setting up configuration for final image output
        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    setLensFacing(CameraX.LensFacing.FRONT)
                    setTargetAspectRatio(screenAspectRatio)
                    setTargetRotation(dialog.texture.display.rotation)
                    setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        //Initializing shutter button
        dialog.click.setOnClickListener {
            //This would avoid dialog from dismissing while processing image
            dialog.setCancelable(false)

            //Creating the image file
            val file = File(activity.getExternalFilesDir(Constants.IMAGES_FOLDER), "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file,
                    object : ImageCapture.OnImageSavedListener {
                        override fun onError(error: ImageCapture.UseCaseError, message: String, exc: Throwable?) {
                            //Making dialog cancellable if the image doesn't gets captured
                            dialog.setCancelable(true)
                            Utils().snackBar(dialog.root_view, activity.getString(R.string.capture_failed))
                        }

                        override fun onImageSaved(file: File) {
                            //Playing shutter sound for feedback
                            soundPool.play(shutterSound, 1F, 1F,0,0, 1F)

                            //Sending the image for compression
                            compressAndSendCallback(file)
                        }
                    })

        }

        CameraX.bindToLifecycle(activity as LifecycleOwner, preview, imageCapture)
    }

    private fun updateTransform() {
        val matrix = Matrix()
        val centerX = dialog.texture.width / 2f
        val centerY = dialog.texture.height / 2f

        val rotationDegrees = when (dialog.texture.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)
        dialog.texture.setTransform(matrix)
    }

    private fun compressAndSendCallback(file : File){
        val loaderDialog = LoaderDialog(activity)
        loaderDialog.show()

        GlobalScope.launch {
            //Lets compress it
            Compressor(activity)
                .setQuality(30)
                .setCompressFormat(Bitmap.CompressFormat.PNG)
                .compressToFile(file)

            //Stopping the camera by unbinding from its lifecycle
            CameraX.unbindAll()

            //Dismissing loader
            loaderDialog.dismiss()

            //Sending callback with compressed file
            onImageCaptured?.onCaptured(file)
        }
    }

    interface OnImageCaptured {
        fun onCaptured(file : File)
    }

    fun setOnImageCapturedListener(onImageCaptured: OnImageCaptured) {
        this.onImageCaptured = onImageCaptured
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



