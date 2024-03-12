package com.example.myapplication

//SplashActivity.kt

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DISPLAY_LENGTH = 500 // 0.5초(500ms)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 일정 시간(SPLASH_DISPLAY_LENGTH) 후에 메인 액티비티로 전환
        Handler().postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish() // 현재 액티비티 종료
        }, SPLASH_DISPLAY_LENGTH.toLong())
    }
}