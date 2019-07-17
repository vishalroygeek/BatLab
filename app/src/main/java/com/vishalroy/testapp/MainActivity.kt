package com.vishalroy.testapp

import android.Manifest.permission
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraX
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vishalroy.testapp.Dialogs.CameraDialog
import com.vishalroy.testapp.Helpers.Typefaces
import com.vishalroy.testapp.Helpers.Utils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

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

        //Initializing ccp
        ccp.registerCarrierNumberEditText(user_number)

        //Initializing click listeners
        submit_btn.setOnClickListener(this)

        //Asking for the required permissions
        askPermissions()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.submit_btn ->
                CameraDialog(this).show()

        }
    }

    private fun askPermissions(){
        Dexter.withActivity(this)
            .withPermissions(
                permission.CAMERA,
                permission.READ_EXTERNAL_STORAGE)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (!report.areAllPermissionsGranted()) {
                        Utils().snackBar(root_view, getString(R.string.permission_warning))
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken) {

                    //Closing app when required permissions aren't granted
                    Utils().toast(this@MainActivity, getString(R.string.permission_permanently_denied))
                    this@MainActivity.finish()
                }
            }).check()
    }
}
