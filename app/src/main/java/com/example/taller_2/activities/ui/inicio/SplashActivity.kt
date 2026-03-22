package com.example.taller_2.activities.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.taller_2.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        //Delay de 3 segundos y redireccionamiento (3000)
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, InicioActivity::class.java))
            finish()
        }, 3000)

    }
}