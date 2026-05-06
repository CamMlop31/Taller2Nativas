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

        // GOOGLE SIGN-IN (SIN CAMBIOS)
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

                    //Verificar si es el primer login

                    val user = SupabaseClient.client.auth.currentUserOrNull()
                    if (user != null) {
                        val existe = UsuarioRepository.existeUsuario(user.id)
                        if (!existe) {
                            val nombreCompleto = user.userMetadata
                                ?.get("full_name").toString()
                                ?.replace("\"", "")?: ""
                            val partes = nombreCompleto.split(" ")
                            val nombres = partes.firstOrNull()?:""
                            val apellidos = partes.drop(1).joinToString(" ")
                            val correoGoogle = user.email ?: ""
                            UsuarioRepository.insertarUsuario(
                                user.id,
                                nombres,
                                apellidos,
                                correoGoogle
                            )
                        }
                    }

                    //Se deben guardar las credenciales para habilitar el ingreso con huella

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

        val lnink = findViewById<TextView>(R.id.linkRegistro)

        lnink.setOnClickListener {
            val intent = Intent(this@Login, Registro::class.java)
            startActivity(intent)
            finish()
        }

        // GOOGLE SIGN-IN
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
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)

                val idToken = account.idToken ?: return

                lifecycleScope.launch {
                    SupabaseClient.client.auth.signInWith(IDToken) {
                        this.idToken = idToken
                        provider = Google
                    }
                    irAPantallaPrincipal()
                }

            } catch (e: Exception) {
                Log.e("GOOGLE_LOGIN", "Error", e)
            }
        }
    }

    // Implementacion de la huella

    private fun configurarVisibilidadHuella() {
        val huellaActiva = CredencialesManager.huellaActiva(this)

        val biometricManager = BiometricManager.from(this)
        val biometriaDisponible =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            ) == BiometricManager.BIOMETRIC_SUCCESS

        tvHuella.visibility =
            if (huellaActiva && biometriaDisponible)
                View.VISIBLE
            else
                View.GONE
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
                            try {
                                SupabaseClient.client.auth.signInWith(Email) {
                                    email = correo
                                    password = contrasena
                                }
                                irAPantallaPrincipal()
                            } catch (e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(
                                        this@Login,
                                        "Error al iniciar sesión: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@Login,
                            "Sesión expirada. Inicia sesión con tu correo.",
                            Toast.LENGTH_LONG
                        ).show()

                        CredencialesManager.limpiarCredenciales(this@Login)
                        configurarVisibilidadHuella()
                    }
                }

                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    if (
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        Toast.makeText(
                            this@Login,
                            "Error biométrico: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(
                        this@Login,
                        "Huella no reconocida, intenta de nuevo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Acceso con huella")
            .setSubtitle("Usa tu huella dactilar para ingresar")
            .setNegativeButtonText("Cancelar")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    // ===== FUNCIÓN EXIGIDA POR EL DOCUMENTO =====
    private fun irAPantallaPrincipal() {
        runOnUiThread {
            startActivity(Intent(this@Login, MainActivity::class.java))
            finishAffinity()
        }
    }
}