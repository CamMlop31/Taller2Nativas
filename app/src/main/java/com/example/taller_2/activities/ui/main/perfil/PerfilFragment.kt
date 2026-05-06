package com.example.taller_2.activities.ui.main.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.example.taller_2.R
import com.example.taller_2.data.UsuarioRepository
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private lateinit var ivFoto: ImageView
    private lateinit var tvNombre: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var tvRol: TextView
    private lateinit var btnEditar: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ivFoto = view.findViewById(R.id.iv_foto_perfil)
        tvNombre = view.findViewById(R.id.tv_perfil_nombre)
        tvCorreo = view.findViewById(R.id.tv_perfil_correo)
        tvRol = view.findViewById(R.id.tv_perfil_rol)
        btnEditar = view.findViewById(R.id.btn_editar_perfil)

        lifecycleScope.launch {

            val usuario = UsuarioRepository.obtenerUsuarioActual()

            android.util.Log.d(
                "DEBUG_PERFIL",
                "Usuario obtenido: $usuario"
            )

            if (usuario != null) {

                tvNombre.text =
                    "${usuario.nombre ?: ""} ${usuario.apellidos ?: ""}"

                tvCorreo.text =
                    usuario.correo ?: "Sin correo"

                tvRol.text =
                    usuario.rol ?: "cliente"

                if (!usuario.foto_url.isNullOrEmpty()) {

                    ivFoto.load(usuario.foto_url) {
                        transformations(CircleCropTransformation())
                        placeholder(R.mipmap.ic_launcher_round)
                        error(R.mipmap.ic_launcher_round)
                    }
                }
            }
        }

        btnEditar.setOnClickListener {

            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    editarPerfilFragment()
                )
                .addToBackStack(null)
                .commit()
        }
    }
}