package com.breezemobilearndemo
import android.app.Application
import com.google.firebase.FirebaseApp
import com.marcinmoskala.kotlinpreferences.PreferenceHolder


class LMSApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.initAppDatabase(this)
        FirebaseApp.initializeApp(this)
        PreferenceHolder.setContext(applicationContext)
    }
}
