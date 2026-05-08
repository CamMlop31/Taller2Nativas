package com.example.taller_2.activities.ui.main.perfil

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.taller_2.R
import com.example.taller_2.activities.SupabaseClient
import com.example.taller_2.data.UsuarioRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.io.File

class editarPerfilFragment : Fragment() {

    private var uriFotoSeleccionada: Uri? = null
    private lateinit var ivEditarFoto: ImageView
    private lateinit var archivoFotoTemp: File

    // ==========================
    // PERMISOS / CÁMARA (SIN CAMBIOS)
    // ==========================

    private val lanzadorPermisoCamara =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { concedido ->
            if (concedido) {
                abrirCamara()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Se necesita permiso de cámara para tomar fotos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val lanzadorCamara =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
            if (exito) {
                uriFotoSeleccionada = Uri.fromFile(archivoFotoTemp)
                ivEditarFoto.load(uriFotoSeleccionada) {
                    transformations(CircleCropTransformation())
                }
            }
        }

    private val lanzadorGaleria =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                uriFotoSeleccionada = uri
                ivEditarFoto.load(uri) {
                    transformations(CircleCropTransformation())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_editar_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivEditarFoto        = view.findViewById(R.id.iv_editar_foto)
        val ivCamaraIcon    = view.findViewById<ImageView>(R.id.iv_camara_icon)

        val etNombres       = view.findViewById<EditText>(R.id.Nombre)
        val etApellidos     = view.findViewById<EditText>(R.id.Apellidos)
        val etGenero        = view.findViewById<EditText>(R.id.G_nero)
        val etEdad          = view.findViewById<EditText>(R.id.Edad)
        val etFchNacimiento = view.findViewById<EditText>(R.id.Fch_nacimiento)
        val etEpsAfilia     = view.findViewById<EditText>(R.id.Eps_afilia)
        val etDireccion     = view.findViewById<EditText>(R.id.Direccion)
        val etCiudad        = view.findViewById<EditText>(R.id.Ciudad)
        val etTelefono      = view.findViewById<EditText>(R.id.Telefono)
        val etEmail         = view.findViewById<EditText>(R.id.Email)
        val etContra        = view.findViewById<EditText>(R.id.Contra)
        val etReContra      = view.findViewById<EditText>(R.id.ReContra)

        val btnGuardar      = view.findViewById<Button>(R.id.Btn_guardarCambios)

        // CARGA COMPLETA DE PERFIL

        lifecycleScope.launch {

            val usuario        = UsuarioRepository.obtenerUsuarioActual()
            val datosUsuario   = UsuarioRepository.obtenerDatosUsuario()
            val datosUbicacion = UsuarioRepository.obtenerDatosUbicacion()

            usuario?.let {
                etNombres.setText(it.nombre ?: "")
                etApellidos.setText(it.apellidos ?: "")
                etEmail.setText(it.correo ?: "")

                if (!it.foto_url.isNullOrEmpty()) {
                    ivEditarFoto.load(it.foto_url) {
                        transformations(CircleCropTransformation())
                        placeholder(R.mipmap.ic_launcher_round)
                        error(R.mipmap.ic_launcher_round)
                    }
                }
            }

            datosUsuario?.let {
                etGenero.setText(it.genero ?: "")
                etEdad.setText(it.edad?.toString() ?: "")
                etFchNacimiento.setText(it.fecha_nacimiento ?: "")
                etEpsAfilia.setText(it.eps_afilia ?: "")
            }

            datosUbicacion?.let {
                etDireccion.setText(it.direccion ?: "")
                etCiudad.setText(it.ciudad ?: "")
                etTelefono.setText(it.telefono ?: "")
            }

            // Seguridad: contraseñas siempre vacías
            etContra.setText("")
            etReContra.setText("")
        }

        ivCamaraIcon.setOnClickListener { mostrarOpcionesFoto() }

        btnGuardar.setOnClickListener {
            guardarCambios(
                etNombres,
                etApellidos,
                etEmail,
                etContra,
                etReContra
            )
        }
    }

    // FOTO (SIN CAMBIOS)
    private fun mostrarOpcionesFoto() {
        val opciones = arrayOf("Tomar foto", "Elegir de galería")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Foto de perfil")
            .setItems(opciones) { _, cual ->
                when (cual) {
                    0 -> verificarPermisoCamara()
                    1 -> lanzadorGaleria.launch("image/*")
                }
            }
            .show()
    }

    private fun verificarPermisoCamara() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> abrirCamara()

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Permiso de cámara")
                    .setMessage("Necesitamos acceso a la cámara para que puedas tomar tu foto de perfil.")
                    .setPositiveButton("Entendido") { _, _ ->
                        lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

            else -> lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
        }
    }

    private fun abrirCamara() {
        val carpeta = File(requireContext().cacheDir, "images")
        carpeta.mkdirs()
        archivoFotoTemp = File(carpeta, "foto_perfil_temp.jpg")

        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            archivoFotoTemp
        )
        lanzadorCamara.launch(uri)
    }

    // GUARDADO (SIN CAMBIOS)

    private fun guardarCambios(
        etNombres: EditText,
        etApellidos: EditText,
        etCorreo: EditText,
        etContrasena: EditText,
        etReContrasena: EditText
    ) {

        val nombres = etNombres.text.toString().trim()
        val apellidos = etApellidos.text.toString().trim()
        val correo = etCorreo.text.toString().trim()
        val contrasena = etContrasena.text.toString()
        val reContrasena = etReContrasena.text.toString()

        val genero = view?.findViewById<EditText>(R.id.G_nero)?.text.toString().trim()
        val edadTexto = view?.findViewById<EditText>(R.id.Edad)?.text.toString().trim()
        val fechaNacimiento = view?.findViewById<EditText>(R.id.Fch_nacimiento)?.text.toString().trim()
        val epsAfilia = view?.findViewById<EditText>(R.id.Eps_afilia)?.text.toString().trim()

        val direccion = view?.findViewById<EditText>(R.id.Direccion)?.text.toString().trim()
        val ciudad = view?.findViewById<EditText>(R.id.Ciudad)?.text.toString().trim()
        val telefono = view?.findViewById<EditText>(R.id.Telefono)?.text.toString().trim()

        val edad = edadTexto.toIntOrNull()

        // VALIDACIONES

        if (nombres.isEmpty() || apellidos.isEmpty() || correo.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Nombres, apellidos y correo son obligatorios",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (edadTexto.isNotEmpty() && edad == null) {
            Toast.makeText(
                requireContext(),
                "Edad inválida",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (contrasena.isNotEmpty()) {
            if (contrasena.length < 6 || contrasena != reContrasena) {
                Toast.makeText(
                    requireContext(),
                    "Revisa la contraseña",
                    Toast.LENGTH_SHORT
                ).show()
                return
            }
        }

        // GUARDADO EN SUPABASE

        lifecycleScope.launch {
            try {

                // Subir foto si aplica
                var fotoUrl: String? = null
                if (uriFotoSeleccionada != null) {
                    fotoUrl = UsuarioRepository.subirFotoPerfil(
                        requireContext(),
                        uriFotoSeleccionada!!
                    )
                }

                // Actualizar tabla Usuarios
                UsuarioRepository.actualizarPerfil(
                    nombres = nombres,
                    apellidos = apellidos,
                    correo = correo,
                    fotoUrl = fotoUrl
                )

                // Guardar / actualizar DatosUsuarios
                if (
                    genero.isNotEmpty() ||
                    edad != null ||
                    fechaNacimiento.isNotEmpty() ||
                    epsAfilia.isNotEmpty()
                ) {
                    UsuarioRepository.guardarDatosUsuario(
                        genero = genero,
                        edad = edad ?: 0,
                        fechaNacimiento = fechaNacimiento,
                        epsAfilia = epsAfilia
                    )
                }

                // Guardar / actualizar DatosUbicacion
                if (
                    direccion.isNotEmpty() ||
                    ciudad.isNotEmpty() ||
                    telefono.isNotEmpty()
                ) {
                    UsuarioRepository.guardarDatosUbicacion(
                        direccion = direccion,
                        ciudad = ciudad,
                        telefono = telefono
                    )
                }

                // Actualizar contraseña (SI el usuario ingresó una nueva)
                if (contrasena.isNotEmpty()) {
                    SupabaseClient.client.auth.updateUser {
                        password = contrasena
                    }
                }

                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Perfil actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    parentFragmentManager.popBackStack()
                }

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}