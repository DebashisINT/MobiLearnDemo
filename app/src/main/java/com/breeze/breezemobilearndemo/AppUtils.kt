package com.breezemobilearndemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.SystemClock
import android.provider.CalendarContract
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.telephony.TelephonyManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.app.ActivityCompat
import java.io.*
import java.math.BigDecimal
import java.security.SecureRandom
import java.sql.Timestamp
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import android.util.Base64
import androidx.annotation.RequiresApi
import javax.crypto.spec.SecretKeySpec

class AppUtils {
    companion object {

        var isRevisit: Boolean? = false

        var isShopAdded = false

        var notificationChannelId = "fts_1"
        var notificationChannelName = "FTS Channel"

        fun getCurrentDateTimeNew(): String {
            val df = LocalDateTime.now()
            var formatD = df.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            return formatD.toString()
        }


        fun getCurrentTimeWithMeredian(): String {
            val df = SimpleDateFormat("h:mm a", Locale.ENGLISH)
            return df.format(Date()).toString()
        }


        fun getCurrentDateTime(): String {
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            return df.format(Date()).toString()
        }

        fun getCurrentTime(): String {
            val df = SimpleDateFormat("HH:mm:ss", Locale.ENGLISH)
            return df.format(Date()).toString()
        }

        @Throws(ParseException::class)
        fun getFormatedDateNew(date: String?, initDateFormat: String?, endDateFormat: String?): String? {
            if(date.equals(""))
                return ""
            val initDate: Date = SimpleDateFormat(initDateFormat).parse(date)
            val formatter = SimpleDateFormat(endDateFormat)
            return formatter.format(initDate)
        }


        fun getCurrentDateyymmdd(): String {
            val c = Calendar.getInstance(Locale.ENGLISH)
            System.out.println("Current time => " + c.time)

            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val formattedDate = df.format(c.time)
            return formattedDate.toString()
        }

        fun getCurrentDateChanged(): String {
            val c = Calendar.getInstance(Locale.ENGLISH)

            val df = SimpleDateFormat("dd-MMM-yy", Locale.ENGLISH)
            val formattedDate = df.format(c.time)
            return formattedDate
        }

        fun isOnline(mContext: Context): Boolean {
            try{
                val cm = mContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                var info: NetworkInfo? = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                // test for connection for WIFI
                if (info != null && info.isAvailable && info.isConnected) {
                    return true
                }
                info = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                // test for connection for Mobile
                return info != null && info.isAvailable && info.isConnected
            }catch (ex:Exception){
                return false
            }
        }


        fun hideSoftKeyboard(activity: Activity) {
            try {
                val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }


        fun getOneDayPreviousDate(dateString: String): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.time = dateFormat.parse(dateString)
            cal.add(Calendar.DATE, -1)
            return dateFormat.format(cal.time) //your formatted date here
        }


        fun getCurrentDateForShopActi(): String {
            val df = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            return df.format(Date()).toString()
        }


        fun getVersionName(context: Context): String {
            var versionName = ""
            try {
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0) as PackageInfo
                versionName = packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
            return "Version " + versionName

        }



        fun hiFirstNameText() : String {
            try{
                if (!Pref.user_name.isNullOrEmpty()){
                    val firstName = Pref.user_name?.substring(0, Pref.user_name?.indexOf(" ")!!)
                    return "Hi $firstName"
                }else {
                    return "Hi"
                }
            }catch (ex:Exception){
                if (!Pref.user_name.isNullOrEmpty()){
                    return "Hi"
                }else {
                    return "Hi ${Pref.user_name}"
                }
            }

        }

        fun getAndroidVersion(): String {
            when(Build.VERSION.SDK_INT) {
                Build.VERSION_CODES.BASE, Build.VERSION_CODES.BASE_1_1 -> {
                    return "Base"
                }
                Build.VERSION_CODES.CUPCAKE -> {
                    return "Cupcake"
                }
                Build.VERSION_CODES.DONUT -> {
                    return "Donut"
                }
                Build.VERSION_CODES.ECLAIR, Build.VERSION_CODES.ECLAIR_0_1, Build.VERSION_CODES.ECLAIR_MR1 -> {
                    return "Eclair"
                }
                Build.VERSION_CODES.FROYO -> {
                    return "Froyo"
                }
                Build.VERSION_CODES.GINGERBREAD, Build.VERSION_CODES.GINGERBREAD_MR1 -> {
                    return "Gingerbread"
                }
                Build.VERSION_CODES.HONEYCOMB, Build.VERSION_CODES.HONEYCOMB_MR1, Build.VERSION_CODES.HONEYCOMB_MR2 -> {
                    return "Honeycomb"
                }
                Build.VERSION_CODES.ICE_CREAM_SANDWICH, Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 -> {
                    return "Ice Cream Sandwich"
                }
                Build.VERSION_CODES.JELLY_BEAN, Build.VERSION_CODES.JELLY_BEAN_MR1, Build.VERSION_CODES.JELLY_BEAN_MR2 -> {
                    return "Jelly Bean"
                }
                Build.VERSION_CODES.KITKAT, Build.VERSION_CODES.KITKAT_WATCH -> {
                    return "Kitkat"
                }
                Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1 -> {
                    return "Lollipop"
                }
                Build.VERSION_CODES.M -> {
                    return "Marshmallow"
                }
                Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1 -> {
                    return "Nougat"
                }
                Build.VERSION_CODES.O, Build.VERSION_CODES.O_MR1 -> {
                    return "Oreo"
                }
                28 -> {
                    return "Pie"
                }
                29 -> {
                    return "Q"
                }
                30 -> {
                    return "11"
                }
                31 -> {
                    return "12"
                }
                else -> {
                    return Build.VERSION.SDK_INT.toString()
                }
            }
        }
             var isFromOrderToshowSchema = false


        @RequiresApi(Build.VERSION_CODES.Q)
        fun deleteApkWithMediaStore(context: Context, fileName: String) {
            val resolver: ContentResolver = context.contentResolver
            val uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI

            val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(fileName)

            resolver.delete(uri, selection, selectionArgs)
        }


        fun installAndDeleteAPK(filePath: String, context: Context) {
            val apkFile = File(filePath)

            if (apkFile.exists()) {
                // Create an intent to install the APK
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }

                context.startActivity(intent)

                // Delete APK after installation delay
                Thread {
                    try {
                        // Wait for some time to let installation complete
                        Thread.sleep(5000)
                        if (apkFile.exists()) {
                            apkFile.delete()
                            println("APK file deleted successfully")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }.start()
            } else {
                println("APK file does not exist")
            }
        }


    }
}