package com.datricle.datriclerun.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.datricle.datriclerun.R

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        var linearLayout: LinearLayout? = null
        var bottomAnim: Animation? = null

        val SPLASH_TIMER = 1000

        //Hooks
        linearLayout = findViewById(R.id.spLinear)

        //Animations
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim)

        //set animations on elements
        linearLayout.animation = bottomAnim
        Handler().postDelayed({
            val intent = Intent(this@SplashScreen, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, SPLASH_TIMER.toLong())
    }
}