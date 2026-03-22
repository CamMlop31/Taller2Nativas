package com.example.taller_2.activities.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller_2.R

class Registro : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_registro)

        //Se llama la vita del registro para afectar la vista directamente

        val rootView = findViewById<ViewGroup>(R.id.registro)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            val bottomPadding = maxOf(systemBars.bottom, imeInsets.bottom)

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                bottomPadding
            )
            insets

        }

        //Botón siguiente del registro de datos personales al registro de datos de ubicacion

        val btn: Button = findViewById(R.id.sigui)

        btn.setOnClickListener {
            val intent = Intent(this@Registro, Registro2::class.java)
            startActivity(intent)
            finish()
        }

    }
}