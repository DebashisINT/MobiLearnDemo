package com.breezemobilearndemo

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.breezemobilearndemo.api.LoginRepositoryProvider
import com.breezemobilearndemo.databinding.ActivityLoginBinding
import com.vmadalin.easypermissions.EasyPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    var binding: ActivityLoginBinding? = null
    val loginView get() = binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginView.root)
        getIMEI()
        initView()
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

        checkPermission()
        loginView.loginTV.setOnClickListener(this)
        loginView.cbRememberMe .isChecked = Pref.isRememberMe

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

                        if (Pref.temp_user_id == loginResponse.user_details!!.user_id) {
                            doAfterLoginFunctionality(loginResponse)
                        }
                        else {
                            doAfterLoginFunctionality(loginResponse)
                        }

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