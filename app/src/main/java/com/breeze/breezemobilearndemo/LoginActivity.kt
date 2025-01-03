package com.breezemobilearndemo

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.breeze.breezemobilearndemo.api.ConfigFetchResponseModel
import com.breezefieldsalesdemo.features.login.api.global_config.ConfigFetchRepoProvider
import com.breezefieldsalesdemo.features.login.api.user_config.UserConfigRepoProvider
import com.breezefieldsalesdemo.features.login.model.userconfig.UserConfigResponseModel
import com.breezemobilearndemo.api.LoginRepositoryProvider
import com.breezemobilearndemo.databinding.ActivityLoginBinding
import com.vmadalin.easypermissions.EasyPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import android.Manifest
import android.content.ContentResolver
import android.os.Environment
import android.provider.MediaStore
import java.io.File

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    var binding: ActivityLoginBinding? = null
    val loginView get() = binding!!
    private var isPasswordVisible = false // Track the visibility status of the password
    private val STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions at runtime


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                this.startActivity(intent)
            }
        }

        requestStoragePermissions()


        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginView.root)
        getIMEI()
        initView()
    }

    private fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
        } else {
            deleteFile_("mobilearn.apk")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                deleteFile_("mobilearn.apk")
            } else {
                Log.d("LoginActivity", "Storage permission denied")
            }
        }
    }

    private fun deleteFile_(fileName: String) {
        val isDeleted = deleteFileFromDownloads(this, fileName)
        if (isDeleted) {
            println("File deleted successfully")
        } else {
            println("File not found or could not be deleted")
        }
    }

    fun deleteFileFromDownloads(context: Context, fileName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped storage approach using MediaStore for Android 10+
            val contentResolver: ContentResolver = context.contentResolver
            val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=?"
            val selectionArgs = arrayOf(fileName)

            try {
                // Attempt to delete the file
                val deletedRows = contentResolver.delete(uri, selection, selectionArgs)
                deletedRows > 0 // Returns true if rows were deleted
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            // Legacy approach for Android 9 and below
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadDir, fileName)

            if (file.exists()) {
                file.delete() // Returns true if the file was deleted
            } else {
                false // File does not exist
            }
        }
    }

    private fun getIMEI() {

        try {
            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

            if (Build.VERSION.SDK_INT >= 29) {

                Pref.imei = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

            } else {
                Pref.imei = telephonyManager.deviceId
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initView() {
        loginView.passwordEDT.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        checkPermission()
        loginView.loginTV.setOnClickListener(this)
        loginView.passwordEyeIV.setOnClickListener(this)
        loginView.cbRememberMe .isChecked = Pref.isRememberMe

        loginView.passwordEyeIV.setOnClickListener {
            isPasswordVisible = !isPasswordVisible // Toggle the visibility status
            togglePasswordVisibility(isPasswordVisible)
        }

        if (Pref.isRememberMe) {
            loginView.usernameEDT.setText(Pref.PhnNo)
            loginView.passwordEDT.setText(Pref.pwd)
        }

        loginView.cbRememberMe.setOnCheckedChangeListener { buttonView, isChecked ->
            loginView.cbRememberMe.isChecked = isChecked
            Pref.isRememberMe = isChecked
            if (!Pref.isRememberMe) {
                Pref.PhnNo = ""
                Pref.pwd = ""
            }
        }

    }

    private fun togglePasswordVisibility(isVisible: Boolean) {
        if (isVisible) {
            // Show password
            loginView.passwordEDT.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            loginView.passwordEyeIV.setImageResource(R.drawable.password_eye)
             // Update the eye icon to open
        } else {
            // Hide password
            loginView.passwordEDT.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            loginView.passwordEyeIV.setImageResource(R.drawable.eye)
            // Update the eye icon to closed
        }

        // Move the cursor to the end of the password field after visibility toggle
        loginView.passwordEDT.setSelection(loginView.passwordEDT.text!!.length)
    }

    private fun checkPermission() {
        var permissionL: ArrayList<String> = ArrayList()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)))
                permissionL.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermission(permissionL.toTypedArray(), 999, "Allow Permissions.")
    }
    private fun requestPermission(permissionList: Array<String>, reqCode: Int, msg: String) {
        EasyPermissions.requestPermissions(this, msg, reqCode, *permissionList)
    }

    private fun hasPermission(permissionList: Array<String>) = EasyPermissions.hasPermissions(this, *permissionList)

    override fun onClick(v: View?) {

        when (v?.id) {
            loginView.loginTV.id -> {
                binding!!.progressWheel.spin()
                try {
                    var usrStr = loginView.usernameEDT.text.toString()
                    var passStr = loginView.passwordEDT.text.toString()
                    if(usrStr.length == 0){
                        binding!!.progressWheel.stopSpinning()
                        Toast.makeText(this, "Enter username", Toast.LENGTH_SHORT).show()
                        loginView.usernameEDT.requestFocus()
                        return
                    }else if(passStr.length == 0){
                        binding!!.progressWheel.stopSpinning()
                        Toast.makeText(this, "Enter password", Toast.LENGTH_SHORT).show()
                        loginView.passwordEDT.requestFocus()
                        return
                    }
                    else{
                        callApi(loginView.usernameEDT.text.toString().trim(), loginView.passwordEDT.text.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun callApi(username: String, password: String) {
        if (Pref.isRememberMe) {
            Pref.PhnNo = username
            Pref.pwd = password
        } else {
            Pref.PhnNo = ""
            Pref.pwd = ""
        }
        Pref.loginID = username

        Pref.logId = username
        Pref.loginPassword = password

        val repository = LoginRepositoryProvider.provideLoginRepository()
        DashboardActivity.compositeDisposable.add(
            repository.login(username, password,
               AppUtils.getVersionName(this), Pref.deviceToken)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                   this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
                    val loginResponse = result as LoginResponse
                    binding!!.progressWheel.stopSpinning()

                    if (loginResponse.status == NetworkConstant.SUCCESS) {
                        callUserConfigApi(loginResponse)

                        /*if (Pref.temp_user_id == loginResponse.user_details!!.user_id) {
                            doAfterLoginFunctionality(loginResponse)

                        }
                        else {
                            doAfterLoginFunctionality(loginResponse)
                        }*/

                    }
                    else if (loginResponse.status == "220") {
                        loginView.loginTV.isEnabled = true
                    }
                    else {
                        if(loginResponse.message!!.contains("IMEI",ignoreCase = true))
                        {
                            var realName=""
                            try{
                                realName= Pref.user_name!!.replace("null","")
                            }catch (ex:Exception){
                                realName=""
                            }

                            openDialogPopupIMEI("Hi! $realName ($username)","This device is currently in use by another user. IMEI is blocked. Please reach out to the admin for assistance.")
                        }else{
                            openDialogPopup(loginResponse.message!!)
                        }

                        loginView.loginTV .isEnabled = true
                    }

                },
                    { error ->
                        binding!!.progressWheel.stopSpinning()
                        loginView.loginTV.isEnabled = true
                        error.printStackTrace()
                    })
        )
    }

    private fun callUserConfigApi(loginResponse: LoginResponse) {
        val repository = UserConfigRepoProvider.provideUserConfigRepository()
        DashboardActivity.compositeDisposable.add(
            repository.userConfig(/*Pref.user_id!!*/ loginResponse.user_details!!.user_id.toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->
                    val response = result as UserConfigResponseModel
                    if (response.status == NetworkConstant.SUCCESS) {
                        try {

                            if (response.getconfigure != null && response.getconfigure!!.size > 0) {
                                for (i in response.getconfigure!!.indices) {
                                   /* if (response.getconfigure?.get(i)?.Key.equals("IsUserWiseLMSEnable", ignoreCase = true)) {
                                        Pref.IsUserWiseLMSEnable = response.getconfigure!![i].Value == "1"
                                        if (!TextUtils.isEmpty(response.getconfigure?.get(i)?.Value)) {
                                            Pref.IsUserWiseLMSEnable = response.getconfigure?.get(i)?.Value == "1"
                                        }
                                    }
                                    else*/
                                    if (response.getconfigure?.get(i)?.Key.equals("IsUserWiseLMSFeatureOnly", ignoreCase = true)) {
                                        Pref.IsUserWiseLMSFeatureOnly = response.getconfigure!![i].Value == "1"
                                        if (!TextUtils.isEmpty(response.getconfigure?.get(i)?.Value)) {
                                            Pref.IsUserWiseLMSFeatureOnly = response.getconfigure?.get(i)?.Value == "1"
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    getConfigFetchApi(loginResponse)

                }, { error ->
                    error.printStackTrace()
                    getConfigFetchApi(loginResponse)
                })
        )
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getConfigFetchApi(loginResponse: LoginResponse) {

        val repository = ConfigFetchRepoProvider.provideConfigFetchRepository()
        DashboardActivity.compositeDisposable.add(
            repository.configFetch()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe({ result ->

                    val configResponse = result as ConfigFetchResponseModel
                    if (configResponse.status == NetworkConstant.SUCCESS) {

                        //begin mantis id 0027432 loc_k functionality Puja 08-05-2024 v4.2.7
                        if (configResponse.loc_k != null)
                            Pref.loc_k = configResponse.loc_k!!
                        //end mantis id 0027432 loc_k functionality Puja 08-05-2024  v4.2.7

                        //begin mantis id 0027432 firebase_k functionality Puja 08-05-2024 v4.2.7
                        if (configResponse.firebase_k != null)
                            Pref.firebase_k = "key="+configResponse.firebase_k!!
                        //end mantis id 0027432 firebase_k functionality Puja 08-05-2024  v4.2.7

                        //begin mantis id 0027683 QuestionAfterNoOfContentForLMS functionality Puja 05-08-2024  v4.2.9
                        if (configResponse.QuestionAfterNoOfContentForLMS != null)
                            Pref.QuestionAfterNoOfContentForLMS = configResponse.QuestionAfterNoOfContentForLMS!!
                        //end mantis id 0027683 QuestionAfterNoOfContentForLMS functionality Puja 05-08-2024  v4.2.9


                        //Puja 07-10-2024 mantis 0027717
                        if (configResponse.IsVideoAutoPlayInLMS != null)
                            Pref.IsVideoAutoPlayInLMS = configResponse.IsVideoAutoPlayInLMS!!

                        //Puja 17-10-2024 mantis 0027772
                        if (configResponse.ShowRetryIncorrectQuiz != null)
                            Pref.ShowRetryIncorrectQuiz = configResponse.ShowRetryIncorrectQuiz!!

                        if (Pref.temp_user_id == loginResponse.user_details!!.user_id) {
                                                    doAfterLoginFunctionality(loginResponse)

                                                }
                                                else {
                                                    doAfterLoginFunctionality(loginResponse)
                                                }
                    }

                }, { error ->
                    error.printStackTrace()
                })
        )
    }

    private fun doAfterLoginFunctionality(loginResponse: LoginResponse) {

        Pref.user_id = loginResponse.user_details!!.user_id
        Pref.temp_user_id = loginResponse.user_details!!.user_id
        Pref.user_name = loginResponse.user_details!!.name
        Pref.session_token = loginResponse.session_token
        Pref.login_time = AppUtils.getCurrentTimeWithMeredian()
        Pref.login_date_time = AppUtils.getCurrentDateTime()
        Pref.login_date = AppUtils.getCurrentDateChanged()

        val anim = ActivityOptions.makeCustomAnimation(
            applicationContext,
            android.R.anim.fade_in,
            android.R.anim.fade_out
        ).toBundle()
        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java), anim)
        finish()

    }


    fun openDialogPopup(text:String){
        val simpleDialog = Dialog(this)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_ok)

        try {
            simpleDialog.setCancelable(true)
            simpleDialog.setCanceledOnTouchOutside(false)
            val dialogName = simpleDialog.findViewById(R.id.tv_dialog_ok_name) as AppCompatTextView
            val dialogCross = simpleDialog.findViewById(R.id.tv_dialog_ok_cancel) as ImageView
            dialogName.text = AppUtils.hiFirstNameText()
            dialogCross.setOnClickListener {
                simpleDialog.cancel()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val dialogHeader = simpleDialog.findViewById(R.id.dialog_yes_header_TV) as AppCompatTextView
        dialogHeader.text = text
        val dialogYes = simpleDialog.findViewById(R.id.tv_dialog_yes) as AppCompatTextView
        dialogYes.setOnClickListener({ view ->
            simpleDialog.cancel()
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        })
        simpleDialog.show()
    }

    fun openDialogPopupIMEI(header:String,text:String){
        val simpleDialog = Dialog(this)
        simpleDialog.setCancelable(false)
        simpleDialog.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        simpleDialog.setContentView(R.layout.dialog_ok_imei)
        val dialogHeader = simpleDialog.findViewById(R.id.dialog_yes_header) as AppCompatTextView
        val dialogBody = simpleDialog.findViewById(R.id.dialog_yes_body) as AppCompatTextView
        dialogHeader.text = header
        dialogBody.text = text
        val dialogYes = simpleDialog.findViewById(R.id.tv_dialog_yes) as AppCompatTextView
        dialogYes.setOnClickListener({ view ->
            simpleDialog.cancel()
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        })
        simpleDialog.show()
    }
    
}