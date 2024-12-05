package com.breezemobilearndemo

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.breezemobilearndemo.databinding.FragmentSearchLmsBinding
import com.pnikosis.materialishprogress.ProgressWheel

class SplashActivity : AppCompatActivity() {
    private lateinit var progress_wheel: ProgressWheel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)


        progress_wheel = findViewById(R.id.progress_wheel)
        progress_wheel.spin()

        if (TextUtils.isEmpty(Pref.user_id) || Pref.user_id.isNullOrBlank()) {
            Handler(Looper.getMainLooper()).postDelayed(kotlinx.coroutines.Runnable {
                val anim = ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle()
                startActivity(Intent(this, LoginActivity::class.java),anim)
                progress_wheel.stopSpinning()
                finish()
            }, 2000)

        }else{
                startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()

        }

        progress_wheel.stopSpinning()
    }
}