package com.example.taller_2.activities.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.taller_2.R
import com.example.taller_2.activities.ui.auth.Login

class InicioActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val btn: Button = findViewById(R.id.btnSiguienteLogin)

        btn.setOnClickListener {
            val intent = Intent(this@InicioActivity, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}