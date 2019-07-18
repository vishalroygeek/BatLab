package com.vishalroy.testapp

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.vishalroy.testapp.Dialogs.CameraDialog
import com.vishalroy.testapp.Helpers.Typefaces
import com.vishalroy.testapp.Helpers.Utils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.vishalroy.testapp.Dialogs.LoaderDialog
import com.vishalroy.testapp.Helpers.Constants
import com.vishalroy.testapp.Models.Visitor
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val typefaces = Typefaces(this)
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private var imageFile : File? = null
    private lateinit var loaderDialog : LoaderDialog
    private var verificationId : String? = null
    private var resendToken : PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        showOTPSection(false)


        //Changing typefaces
        title_text.typeface = typefaces.ralewayBold()
        description.typeface = typefaces.dosisMedium()
        timer.typeface = typefaces.dosisMedium()
        user_number.typeface = typefaces.dosisSemiBold()
        otp_view.typeface = typefaces.dosisSemiBold()
        submit_btn.typeface = typefaces.dosisBold()

        //Initializing click listeners
        submit_btn.setOnClickListener(this)

        //Initializing loader dialog
        loaderDialog = LoaderDialog(this)

        //Binding ccp with editText
        ccp.registerCarrierNumberEditText(user_number)

        //Asking for the required permissions
        askPermissions()
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.submit_btn ->{
                if (ccp.isValidFullNumber){
                    if(isOTPVisible()){
                        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp_view.text.toString())
                        signInWithPhoneAuthCredential(credential)
                    }else{
                        checkIfVisitorAlreadyExists(ccp.fullNumber)
                    }
                }else{
                    Utils().snackBar(root_view, getString(R.string.invalid_number))
                }
            }
        }
    }

    private fun checkIfVisitorAlreadyExists(number : String){
        loaderDialog.show()
        Utils().snackBar(root_view, getString(R.string.checking_visitor))

        val visitorQuery = database.child(Constants.FIREBASE_COLLECTION_VISITORS).limitToFirst(1).orderByChild(Constants.VISITOR_NUMBER).equalTo(number)

        visitorQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChildren()){
                    for(singleSnapshot in  dataSnapshot.children){
                        //Visitor already exists
                        //Creating a new visitor object from dataSnapshot
                        val visitor = singleSnapshot.getValue(Visitor::class.java)
                        if (visitor != null) {
                            increaseVisitCount(visitor)
                        }
                    }
                }else{
                    //Visitor doesn't exists
                    loaderDialog.dismiss()

                    //Lets get the picture of the visitor
                    showCameraDialog()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                loaderDialog.dismiss()
                Utils().snackBar(root_view, getString(R.string.went_wrong))
            }
        })
    }

    private fun increaseVisitCount(visitor: Visitor){
        loaderDialog.show()
        Utils().snackBar(root_view, getString(R.string.updating_visit))

        //Increasing the visit_count of the visitor object
        visitor.visit_count = visitor.visit_count+1

        //Now updating the visit_count on database as well
        database.child(Constants.FIREBASE_COLLECTION_VISITORS).child(visitor.id)
            .child(Constants.VISITOR_VISIT_COUNT).setValue(visitor.visit_count)
            .addOnSuccessListener {
                loaderDialog.dismiss()
                showWelcomeDialog(visitor)
            }
            .addOnFailureListener {
                loaderDialog.dismiss()
                Utils().snackBar(root_view, getString(R.string.went_wrong))
            }
    }

    private fun sendOTP(number : String){
        loaderDialog.show()
        Utils().snackBar(root_view, getString(R.string.sending_otp))

        //Sending verification code to phone number
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            "+$number",
            30,
            TimeUnit.SECONDS,
            this,
            callbacks)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            loaderDialog.dismiss()
            Utils().snackBar(root_view, getString(R.string.verification_failed))
            markAsSuspiciousVisitor()
        }

        override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken) {
            loaderDialog.dismiss()
            Utils().snackBar(root_view, getString(R.string.otp_sent))

            //Assigning the params
            this@MainActivity.verificationId = verificationId
            resendToken = token

            //This will update the timer UI every second
            startOTPTimer()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        loaderDialog.show()
        Utils().snackBar(root_view, getString(R.string.signing_in))

        //Lets sign in with the provided credential
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Utils().snackBar(root_view, getString(R.string.verification_succeed))
                    createVisitor()
                } else {
                    loaderDialog.dismiss()
                    Utils().snackBar(root_view, getString(R.string.verification_failed))
                    markAsSuspiciousVisitor()
                }
            }
    }

    private fun createVisitor(){
        Utils().snackBar(root_view, getString(R.string.uploading_image))

        //Generating unique id for visitor
        val visitorId = database.child(Constants.FIREBASE_COLLECTION_VISITORS).push().key

        //Creating image reference before uploading
        val visitorImageRef = storage.child(Constants.STORAGE_VISITOR_IMAGES).child("$visitorId.jpg")

        //Uploading the imageFile which we had fetched earlier
        val uploadTask = visitorImageRef.putFile(Uri.fromFile(imageFile))

        //Using continuation to get download url
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                loaderDialog.dismiss()
                Utils().snackBar(root_view, getString(R.string.image_upload_failed))
                resetActivity()
            }
            return@Continuation visitorImageRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful && task.result!=null) {
                Utils().snackBar(root_view, getString(R.string.registering))

                //Here, we got the download url
                val downloadUrl = task.result

                //Lets create a new visitor and push it to database
                val visitor = Visitor(visitorId!!, ccp.fullNumber, downloadUrl!!.toString(),1)

                database.child(Constants.FIREBASE_COLLECTION_VISITORS).child(visitorId).setValue(visitor)
                    .addOnSuccessListener {
                        loaderDialog.dismiss()
                        showWelcomeDialog(visitor)
                    }
                    .addOnFailureListener {
                        loaderDialog.dismiss()
                        Utils().snackBar(root_view, getString(R.string.went_wrong))
                        resetActivity()
                    }


            } else {
                loaderDialog.dismiss()
                Utils().snackBar(root_view, getString(R.string.image_upload_failed))
                resetActivity()
            }
        }

    }

    private fun markAsSuspiciousVisitor(){
        loaderDialog.show()
        Utils().snackBar(root_view, getString(R.string.uploading_image))

        //Lets hide the OTP section first
        showOTPSection(false)

        //Generating unique id for visitor
        val visitorId = database.child(Constants.FIREBASE_COLLECTION_SUSPICIOUS_VISITORS).push().key

        //Creating image reference before uploading
        val visitorImageRef = storage.child(Constants.STORAGE_VISITOR_IMAGES).child("$visitorId.jpg")

        //Uploading the imageFile which we had fetched earlier
        val uploadTask = visitorImageRef.putFile(Uri.fromFile(imageFile))

        //Using continuation to get download url
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                loaderDialog.dismiss()
                Utils().snackBar(root_view, getString(R.string.image_upload_failed))
                resetActivity()
            }
            return@Continuation visitorImageRef.downloadUrl
        }).addOnCompleteListener { task ->
            if (task.isSuccessful && task.result!=null) {
                Utils().snackBar(root_view, getString(R.string.updating_visit))

                //Here, we got the download url
                val downloadUrl = task.result

                //Lets create a new visitor and push it to database
                val visitor = Visitor(visitorId!!, ccp.fullNumber, downloadUrl!!.toString(),0)

                database.child(Constants.FIREBASE_COLLECTION_SUSPICIOUS_VISITORS).child(visitorId).setValue(visitor)
                    .addOnCompleteListener {
                        loaderDialog.dismiss()

                        //Restarting activity
                        resetActivity()
                    }


            } else {
                loaderDialog.dismiss()
                Utils().snackBar(root_view, getString(R.string.image_upload_failed))
                resetActivity()
            }
        }
    }

    private fun startOTPTimer(){
        val totalTime = 30
        var seconds = 0

        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {
                if (seconds < totalTime){
                    seconds++
                    handler.postDelayed(this, 1000)

                    //Updating timer text
                    timer.text = (totalTime-seconds).toString()+"s"
                }else if (!loaderDialog.dialog.isShowing){
                    //Doing the dialog showing check above to ensure that no process
                    //is going on right now
                    markAsSuspiciousVisitor()
                }
            }
        })
    }

    private fun showWelcomeDialog(visitor: Visitor){
        Utils().snackBar(root_view, visitor.number)
    }

    private fun showCameraDialog(){
        val cameraDialog = CameraDialog(this)
        cameraDialog.setOnImageCapturedListener(object : CameraDialog.OnImageCaptured{
            override fun onCaptured(file: File){
                //Running on main UI thread to avoid crash
                runOnUiThread {
                    cameraDialog.dismiss()
                    user_image.setImageURI(Uri.fromFile(file))

                    //Assigning imageFile to file
                    imageFile = file

                    //Changing the text of button to submit
                    submit_btn.text = getString(R.string.submit)

                    showOTPSection(true)

                    //Lets send the OTP to visitor's number
                    sendOTP(ccp.fullNumber)
                }
            }
        })
        cameraDialog.show()
    }

    private fun showOTPSection(show : Boolean){
        if (!show && otp_view.visibility==View.VISIBLE){
            otp_view.visibility = View.GONE
            timer.visibility = View.GONE
        }else if (show && (otp_view.visibility==View.GONE||otp_view.visibility==View.INVISIBLE)){
            otp_view.visibility = View.VISIBLE
            timer.visibility = View.VISIBLE
        }

        //Disabling editText and ccp when otp section is visible
        user_number.isEnabled = !show
        ccp.isEnabled = !show
    }

    private fun isOTPVisible(): Boolean{
        return otp_view.visibility == View.VISIBLE
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

    private fun resetActivity(){
        //Signing out before resetting activity
        auth.signOut()

        user_image.setImageDrawable(getDrawable(R.drawable.app_logo))
        user_number.setText("")
        otp_view.setText("")
        showOTPSection(false)

        Utils().snackBar(root_view, getString(R.string.activity_reset))
    }
}
