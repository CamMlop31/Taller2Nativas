package com.example.taller_2.activities.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.taller_2.R
import com.example.taller_2.activities.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import org.slf4j.MDC.put

class Registro2 : AppCompatActivity() {

    private lateinit var etDirection: EditText
    private lateinit var etCiuidad: EditText
    private lateinit var etTelefono: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContra: EditText
    private lateinit var etReContra: EditText
    private lateinit var etCheckTerminos: EditText
    private lateinit var etRegistrarse: EditText
    private lateinit var tvRegresarLogin: TextView


    @Serializable
    data class Usuario(
        val id: String,
        val nombre: String,
        val apellidos: String,
        val correo: String
    )

    @Serializable
    data class DatosUsuario(
        val iddatosusu: String,
        val genero: String,
        val edad: Int,
        val fecha_nacimiento: String,
        val eps_afilia: String
    )

    @Serializable
    data class DatosUbicacion(
        val idregistro: String,
        val direccion: String,
        val ciudad: String,
        val telefono: String
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_registro2)

        //Se llama la vista del registro para afectar la vista directamente

        val rootView = findViewById<ViewGroup>(R.id.Registro2)
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

        // Capturar los datos del formulario de registro 1

        val nombre = intent.getStringExtra("nombre") ?: ""
        val apellidos = intent.getStringExtra("apellidos") ?: ""
        val genero = intent.getStringExtra("genero") ?: ""
        val edad = intent.getIntExtra("edad", -1)
        val fechaNacimiento = intent.getStringExtra("fechaNacimiento") ?: ""
        val epsAfilia = intent.getStringExtra("epsAfilia") ?: ""

        // Datos del formulario 2

        val etDireccion = findViewById<EditText>(R.id.Direccion)
        val etCiudad = findViewById<EditText>(R.id.Ciudad)
        val etTelefono = findViewById<EditText>(R.id.Telefono)
        val etEmail = findViewById<EditText>(R.id.Email)
        val etContra = findViewById<EditText>(R.id.Contra)
        val etReContra = findViewById<EditText>(R.id.ReContra)
        val btnRegistrar = findViewById<Button>(R.id.Registrarse)
        val tvRegresarLogin = findViewById<TextView>(R.id.Regre_login)

        btnRegistrar.setOnClickListener {

            val direccion = etDireccion.text.toString().trim()
            val ciudad = etCiudad.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val correo = etEmail.text.toString().trim()
            val contra = etContra.text.toString().trim()
            val reContra = etReContra.text.toString().trim()

            // Validaciones de los campos y del registro

            if (direccion.isEmpty() || ciudad.isEmpty() || telefono.isEmpty() ||
                correo.isEmpty() || contra.isEmpty() || reContra.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (nombre.isEmpty() || apellidos.isEmpty() || genero.isEmpty() ||
                fechaNacimiento.isEmpty() || epsAfilia.isEmpty()
            ) {
                Toast.makeText(
                    this,
                    "Por favor, completa todos los campos",
                    Toast.LENGTH_SHORT
                )
                    .show()
                finish()
                return@setOnClickListener
            }

            if (edad == -1) {
                Toast.makeText(
                    this,
                    "Error con la edad",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                return@setOnClickListener
            }

            if (contra != reContra) {
                Toast.makeText(
                    this,
                    "Las contraseñas no coinciden",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val checkTerminos = findViewById<CheckBox>(R.id.CheckTerminos)

            if (!checkTerminos.isChecked) {
                Toast.makeText(
                    this,
                    "Debes aceptar los términos y condiciones",
                    Toast.LENGTH_SHORT
                )
                    .show()
                return@setOnClickListener
            }

            if (contra.length < 8) {
                Toast.makeText(
                    this,
                    "La contraseña debe tener al menos 8 caracteres",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo inválido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //Regisro de los datos en supabase

            lifecycleScope.launch {
                try {

                    //Paso1: Registrar correo y contraseña en el Auth de supabase

                    SupabaseClient.client.auth.signUpWith(Email) {
                        email = correo
                        password = contra
                        data = buildJsonObject {
                            put("nombre", nombre)
                            put("apellidos", apellidos)
                        }
                    }

                    //Paso 2: UUID y guardar campos adicionales


                    val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                        ?: throw Exception("Usuario no autenticado")

                    SupabaseClient.client.postgrest["Usuarios"].insert(
                        Usuario(
                            id = userId,
                            nombre = nombre,
                            apellidos = apellidos,
                            correo = correo
                        )
                    )

                    SupabaseClient.client.postgrest["DatosUsuarios"].insert(
                        DatosUsuario(
                            iddatosusu = userId,
                            genero = genero,
                            edad = edad,
                            fecha_nacimiento = fechaNacimiento,
                            eps_afilia = epsAfilia
                        )
                    )

                    SupabaseClient.client.postgrest["DatosUbicacion"].insert(
                        DatosUbicacion(
                            idregistro = userId,
                            direccion = direccion,
                            ciudad = ciudad,
                            telefono = telefono
                        )
                    )


                    //Paso 3: Registro Exitoso
                    runOnUiThread {
                        Toast.makeText(
                            this@Registro2,
                            "Registro exitoso",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this@Registro2, Login::class.java))
                        finish()
                    }

                } catch (e: Exception) {
                    runOnUiThread {}
                    Toast.makeText(
                        this@Registro2,
                        "Error en el registro: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            tvRegresarLogin.setOnClickListener {
                startActivity(Intent(this, Login::class.java))
                finish()
            }

        }

    }

}