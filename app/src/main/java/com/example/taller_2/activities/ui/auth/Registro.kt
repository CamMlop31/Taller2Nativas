package com.example.taller_2.activities.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taller_2.R

class Registro : AppCompatActivity() {

    private lateinit var etNombre: EditText
    private lateinit var etApellidos: EditText
    private lateinit var etGenero: EditText
    private lateinit var etEdad: EditText
    private lateinit var etFchNacimiento: EditText
    private lateinit var etEpsAfilia: EditText
    private lateinit var etBtonsigui: Button

    //Se configura la funcionalidad para que el scroll se mueva y no lo tape el teclado

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_registro)

        //Se llama la vista del registro para afectar la vista directamente

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

        //Referenciacion datos

        etNombre = findViewById(R.id.Nombre)
        etApellidos = findViewById(R.id.Apellidos)
        etGenero = findViewById(R.id.G_nero)
        etEdad = findViewById(R.id.Edad)
        etFchNacimiento = findViewById(R.id.Fch_nacimiento)
        etEpsAfilia = findViewById(R.id.Eps_afilia)
        etBtonsigui = findViewById(R.id.Btn_sigui)

        //Escucha del Botón siguiente del registro de datos de ubicacion al inicio

        etBtonsigui.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val apellidos = etApellidos.text.toString().trim()
            val genero = etGenero.text.toString().trim()
            val edadTexto = etEdad.text.toString().trim()
            val edad = edadTexto.toIntOrNull()
            val fechaNacimiento = etFchNacimiento.text.toString().trim()
            val epsAfilia = etEpsAfilia.text.toString().trim()

            //Validaciones de los campos del formulario

            if (nombre.isEmpty() || apellidos.isEmpty() || genero.isEmpty() || fechaNacimiento.isEmpty() || epsAfilia.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (edad == null || edad <= 0) {
                Toast.makeText(this, "Ingrese una edad válida", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Solo entra aquí si TODO está bien
            Toast.makeText(this, "Diligencia todo el siguiente formulario", Toast.LENGTH_SHORT).show()

            //Pasar los datos al siguiente formulario

            val intent = Intent(this@Registro, Registro2::class.java)

            intent.putExtra("nombre", nombre)
            intent.putExtra("apellidos", apellidos)
            intent.putExtra("genero", genero)
            intent.putExtra("edad", edad)
            intent.putExtra("fechaNacimiento", fechaNacimiento)
            intent.putExtra("epsAfilia", epsAfilia)

            startActivity(intent)
            finish()
        }

    }
}