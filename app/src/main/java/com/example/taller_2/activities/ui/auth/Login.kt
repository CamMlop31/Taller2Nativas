package com.example.taller_2.activities.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.taller_2.R
import com.example.taller_2.activities.SupabaseClient
import com.example.taller_2.activities.ui.main.MainActivity
import com.example.taller_2.data.CredencialesManager
import com.example.taller_2.data.UsuarioRepository
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.launch

class Login : AppCompatActivity() {

    private lateinit var etCorreo: EditText
    private lateinit var etContra: EditText
    private lateinit var btnIngresar: Button
    private lateinit var tvIngresoGoogle: TextView
    private lateinit var tvHuella: TextView

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_GOOGLE_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_login)

        val rootView = findViewById<ViewGroup>(R.id.login)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                maxOf(systemBars.bottom, imeInsets.bottom)
            )
            insets
        }

        etCorreo = findViewById(R.id.Nombre)
        etContra = findViewById(R.id.Contra)
        btnIngresar = findViewById(R.id.btnIngresar)
        tvIngresoGoogle = findViewById(R.id.tvIngresoGoogle)
        tvHuella = findViewById(R.id.in_huella)

        configurarVisibilidadHuella()
        tvHuella.setOnClickListener { mostrarDialogoHuella() }


        // CONFIGURACIÓN GOOGLE

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("195889917013-vfjdc707d6lu5qauhv50qkrbco945b8a.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // LOGIN CON CORREO

        btnIngresar.setOnClickListener {

            val correo = etCorreo.text.toString().trim()
            val contra = etContra.text.toString().trim()

            if (correo.isEmpty() || contra.isEmpty()) return@setOnClickListener

            lifecycleScope.launch {
                try {
                    SupabaseClient.client.auth.signInWith(Email) {
                        email = correo
                        password = contra
                    }

                    val user = SupabaseClient.client.auth.currentUserOrNull()
                    if (user != null && !UsuarioRepository.existeUsuario(user.id)) {

                        val fullName = user.userMetadata
                            ?.get("full_name")
                            ?.toString()
                            ?.replace("\"", "") ?: ""

                        val partes = fullName.split(" ")
                        val nombres = partes.firstOrNull() ?: ""
                        val apellidos = partes.drop(1).joinToString(" ")

                        UsuarioRepository.insertarUsuario(
                            id = user.id,
                            nombres = nombres,
                            apellidos = apellidos,
                            correo = user.email ?: ""
                        )
                    }

                    CredencialesManager.guardarCredenciales(
                        this@Login,
                        correo,
                        contra
                    )

                    irAPantallaPrincipal()

                } catch (e: Exception) {
                    Toast.makeText(this@Login, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        findViewById<TextView>(R.id.linkRegistro).setOnClickListener {
            startActivity(Intent(this, Registro::class.java))
            finish()
        }

        // LOGIN GOOGLE

        tvIngresoGoogle.setOnClickListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                startActivityForResult(
                    googleSignInClient.signInIntent,
                    RC_GOOGLE_SIGN_IN
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        configurarVisibilidadHuella()
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            try {
                val account = GoogleSignIn
                    .getSignedInAccountFromIntent(data)
                    .getResult(ApiException::class.java)

                val idToken = account.idToken ?: return

                lifecycleScope.launch {
                    try {
                        SupabaseClient.client.auth.signInWith(IDToken) {
                            this.idToken = idToken
                            provider = Google
                        }

                        // ✅ Guardar usuario en tabla Usuarios si no existe
                        verificarEInsertarUsuarioGoogle()

                        irAPantallaPrincipal()

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@Login,
                            "Error Google: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                Log.e("GOOGLE_LOGIN", "Error", e)
            }
        }
    }

    private suspend fun verificarEInsertarUsuarioGoogle() {

        val user = SupabaseClient.client.auth.currentUserOrNull() ?: return

        if (UsuarioRepository.existeUsuario(user.id)) return

        val fullName = user.userMetadata
            ?.get("full_name")
            ?.toString()
            ?.replace("\"", "") ?: ""

        val partes = fullName.split(" ")
        val nombres = partes.firstOrNull() ?: ""
        val apellidos = partes.drop(1).joinToString(" ")

        UsuarioRepository.insertarUsuario(
            id = user.id,
            nombres = nombres,
            apellidos = apellidos,
            correo = user.email ?: ""
        )
    }

    // BIOMETRÍA

    private fun configurarVisibilidadHuella() {
        val huellaActiva = CredencialesManager.huellaActiva(this)
        val biometricManager = BiometricManager.from(this)
        val disponible =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) == BiometricManager.BIOMETRIC_SUCCESS

        tvHuella.visibility =
            if (huellaActiva && disponible) View.VISIBLE else View.GONE
    }

    private fun mostrarDialogoHuella() {

        val executor = ContextCompat.getMainExecutor(this)

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    val correo =
                        CredencialesManager.obtenerCorreo(this@Login)
                    val contrasena =
                        CredencialesManager.obtenerContrasena(this@Login)

                    if (correo != null && contrasena != null) {
                        lifecycleScope.launch {
                            SupabaseClient.client.auth.signInWith(Email) {
                                email = correo
                                password = contrasena
                            }
                            irAPantallaPrincipal()
                        }
                    }
                }
            }
        )

        biometricPrompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Acceso con huella")
                .setSubtitle("Usa tu huella dactilar para ingresar")
                .setNegativeButtonText("Cancelar")
                .build()
        )
    }

    private fun irAPantallaPrincipal() {
        startActivity(Intent(this@Login, MainActivity::class.java))
        finishAffinity()
    }
}