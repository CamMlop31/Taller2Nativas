package com.example.taller_2.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.taller_2.activities.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.storage.storage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

object UsuarioRepository {

    @Serializable
    data class UsuarioData(
        val id: String = "",
        val nombre: String? = null,
        val apellidos: String? = null,
        val correo: String? = null,
        val rol: String? = "cliente",
        val foto_url: String? = null
    )

    @Serializable
    data class DatosUsuarioData(
        val iddatosusu: String,
        val genero: String? = null,
        val edad: Int? = null,
        val fecha_nacimiento: String? = null,
        val eps_afilia: String? = null
    )

    @Serializable
    data class DatosUbicacionData(
        val idregistro: String,
        val direccion: String? = null,
        val ciudad: String? = null,
        val telefono: String? = null
    )

    // USUARIO PRINCIPAL

    suspend fun existeUsuario(userId: String): Boolean =
        try {
            SupabaseClient.client
                .postgrest["Usuarios"]
                .select(Columns.raw("id")) {
                    filter { eq("id", userId) }
                }
                .decodeList<Map<String, String>>()
                .isNotEmpty()
        } catch (e: Exception) {
            false
        }

    suspend fun obtenerUsuarioActual(): UsuarioData? {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return null

        return try {
            SupabaseClient.client
                .postgrest["Usuarios"]
                .select { filter { eq("id", userId) } }
                .decodeList<UsuarioData>()
                .firstOrNull()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertarUsuario(
        id: String,
        nombres: String,
        apellidos: String,
        correo: String
    ) {
        SupabaseClient.client.postgrest["Usuarios"].insert(
            UsuarioData(
                id = id,
                nombre = nombres,
                apellidos = apellidos,
                correo = correo
            )
        )
    }

    suspend fun obtenerRolActual(): String =
        obtenerUsuarioActual()?.rol ?: "cliente"

    suspend fun actualizarPerfil(
        nombres: String,
        apellidos: String,
        correo: String,
        fotoUrl: String? = null
    ) {

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return

        val datos = buildJsonObject {
            put("nombre", nombres)
            put("apellidos", apellidos)
            put("correo", correo)
            if (fotoUrl != null) put("foto_url", fotoUrl)
        }

        SupabaseClient.client
            .postgrest["Usuarios"]
            .update(datos) {
                filter { eq("id", userId) }
            }
    }


    // DATOS DE USUARIO (INSERT / UPDATE)


    suspend fun guardarDatosUsuario(
        genero: String,
        edad: Int,
        fechaNacimiento: String,
        epsAfilia: String
    ) {

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return

        val existente = obtenerDatosUsuario()

        if (existente == null) {
            SupabaseClient.client.postgrest["DatosUsuarios"].insert(
                DatosUsuarioData(
                    iddatosusu = userId,
                    genero = genero,
                    edad = edad,
                    fecha_nacimiento = fechaNacimiento,
                    eps_afilia = epsAfilia
                )
            )
        } else {
            val datos = buildJsonObject {
                put("genero", genero)
                put("edad", edad)
                put("fecha_nacimiento", fechaNacimiento)
                put("eps_afilia", epsAfilia)
            }

            SupabaseClient.client.postgrest["DatosUsuarios"].update(datos) {
                filter { eq("iddatosusu", userId) }
            }
        }
    }

    suspend fun obtenerDatosUsuario(): DatosUsuarioData? {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return null

        return SupabaseClient.client
            .postgrest["DatosUsuarios"]
            .select { filter { eq("iddatosusu", userId) } }
            .decodeList<DatosUsuarioData>()
            .firstOrNull()
    }


    // DATOS DE UBICACIÓN (INSERT / UPDATE)


    suspend fun guardarDatosUbicacion(
        direccion: String,
        ciudad: String,
        telefono: String
    ) {

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return

        val existente = obtenerDatosUbicacion()

        if (existente == null) {
            SupabaseClient.client.postgrest["DatosUbicacion"].insert(
                DatosUbicacionData(
                    idregistro = userId,
                    direccion = direccion,
                    ciudad = ciudad,
                    telefono = telefono
                )
            )
        } else {
            val datos = buildJsonObject {
                put("direccion", direccion)
                put("ciudad", ciudad)
                put("telefono", telefono)
            }

            SupabaseClient.client.postgrest["DatosUbicacion"].update(datos) {
                filter { eq("idregistro", userId) }
            }
        }
    }

    suspend fun obtenerDatosUbicacion(): DatosUbicacionData? {
        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return null

        return SupabaseClient.client
            .postgrest["DatosUbicacion"]
            .select { filter { eq("idregistro", userId) } }
            .decodeList<DatosUbicacionData>()
            .firstOrNull()
    }

    // FOTO DE PERFIL


    suspend fun subirFotoPerfil(contexto: Context, uri: Uri): String {

        val userId = SupabaseClient.client.auth.currentUserOrNull()?.id ?: return ""

        val bytes = if (uri.scheme == "content") {
            contexto.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } else {
            File(uri.path!!).readBytes()
        } ?: return ""

        val ruta = "perfil_$userId.jpg"

        SupabaseClient.client.storage["avatars"].upload(
            path = ruta,
            data = bytes,
            options = { upsert = true }
        )

        return SupabaseClient.client
            .storage["avatars"]
            .publicUrl(ruta)
    }
}