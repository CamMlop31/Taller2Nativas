package com.example.taller_2.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.taller_2.R

class Login : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val tv = findViewById<TextView>(R.id.linkRegistro)

        tv.setOnClickListener {
            val intent =Intent(this@Login, Registro::class.java)
            startActivity(intent)
            finish()
        }

    }
}
