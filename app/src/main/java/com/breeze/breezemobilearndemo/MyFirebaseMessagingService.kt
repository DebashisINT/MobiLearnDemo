package com.breezemobilearndemo
import android.content.Intent
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import android.text.TextUtils
import android.view.View
import com.breezemobilearndemo.domain.LMSNotiEntity
import com.google.firebase.messaging.FirebaseMessagingService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        println("MyFirebaseMessagingService onNewToken");

        GlobalScope.launch(Dispatchers.IO) {

            var refreshedToken = token

            while (refreshedToken == null) {
                refreshedToken = token
            }

            withContext(Dispatchers.Main) {

                if (!TextUtils.isEmpty(Pref.user_id)) {

                    GlobalScope.launch(Dispatchers.IO) {

                        callUpdateDeviceTokenApi(refreshedToken)

                        withContext(Dispatchers.Main) {

                        }
                    }
                }

                Pref.deviceToken = token
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        println("Refreshed token onMessageReceived"+remoteMessage);

        if (TextUtils.isEmpty(Pref.user_id)) {

            if (!TextUtils.isEmpty(remoteMessage?.data?.get("type")) && remoteMessage?.data?.get("type") == "clearData") {
                val packageName = applicationContext.packageName
                val runtime = Runtime.getRuntime()
                runtime.exec("pm clear $packageName")
            }

            return
        }

        val body = remoteMessage?.data?.get("body")

        val notification = NotificationUtils(getString(R.string.app_name), "", "", "")

        if (!TextUtils.isEmpty(body)) {
            if(remoteMessage?.data?.get("type").equals("lms_content_assign")){

                try {
                    var obj : LMSNotiEntity = LMSNotiEntity()
                    obj.noti_datetime = AppUtils.getCurrentDateTime()
                    obj.noti_date = AppUtils.getCurrentDateForShopActi()
                    obj.noti_time = AppUtils.getCurrentTime()
                    obj.noti_header = remoteMessage?.data?.get("header").toString()
                    if(obj.noti_header == "null"){
                        obj.noti_header = "New Assignment"
                    }
                    obj.noti_message = remoteMessage?.data?.get("body").toString()
                    obj.isViwed=false
                    AppDatabase.getDBInstance()!!.lmsNotiDao().insert(obj)
                } catch (e: Exception) {
                    e.printStackTrace()
                }


                notification.sendFCMNotificaiton(applicationContext, remoteMessage)

                val intent = Intent()
                intent.action = "FCM_ACTION_RECEIVER"
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            }
        }

        ringtone()
    }

    private fun callUpdateDeviceTokenApi(refreshedToken: String?) {

        if (!AppUtils.isOnline(applicationContext))
            return

        val repository = UpdateDeviceTokenRepoProvider.updateDeviceTokenRepoProvider()

        DashboardActivity.compositeDisposable.add(
                repository.updateDeviceToken(refreshedToken!!)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe({ result ->
                            val response = result as BaseResponse

                        }, { error ->
                            error.printStackTrace()
                        })
        )
    }


    private fun ringtone() {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            val audioManager = applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

}

