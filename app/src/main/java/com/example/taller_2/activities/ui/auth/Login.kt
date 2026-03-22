package com.example.taller_2.activities.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.taller_2.R
import com.example.taller_2.activities.ui.main.MainActivity

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_login)

        val lnink = findViewById<TextView>(R.id.linkRegistro)

        lnink.setOnClickListener {
            val intent = Intent(this@Login, Registro::class.java)
            startActivity(intent)
            finish()
        }

        val btn2: Button = findViewById(R.id.btnIngresar)

        btn2.setOnClickListener {
            val intent = Intent(this@Login, MainActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}