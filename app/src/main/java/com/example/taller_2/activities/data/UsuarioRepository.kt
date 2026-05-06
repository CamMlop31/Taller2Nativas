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

    suspend fun existeUsuario(userId: String): Boolean {

        return try {

            val resultado = SupabaseClient.client
                .postgrest["Usuarios"]
                .select(Columns.raw("id")) {

                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<Map<String, String>>()

            resultado.isNotEmpty()

        } catch (e: Exception) {

            Log.e("DEBUG_QUERY", "Error existeUsuario: ${e.message}")
            false
        }
    }

    suspend fun obtenerUsuarioActual(): UsuarioData? {

        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return null

        Log.d("DEBUG_QUERY", "User ID auth: $userId")

        return try {

            val resultado = SupabaseClient.client
                .postgrest["Usuarios"]
                .select {

                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UsuarioData>()

            Log.d("DEBUG_QUERY", "Resultado completo: $resultado")

            resultado.firstOrNull()

        } catch (e: Exception) {

            Log.e("DEBUG_QUERY", "Error obtenerUsuarioActual: ${e.message}")
            null
        }
    }

    suspend fun insertarUsuario(
        id: String,
        nombres: String,
        apellidos: String,
        correo: String
    ) {

        try {

            SupabaseClient.client
                .postgrest["Usuarios"]
                .insert(
                    UsuarioData(
                        id = id,
                        nombre = nombres,
                        apellidos = apellidos,
                        correo = correo
                    )
                )

            Log.d("DEBUG_QUERY", "Usuario insertado correctamente")

        } catch (e: Exception) {

            Log.e("DEBUG_QUERY", "Error insertarUsuario: ${e.message}")
        }
    }

    suspend fun obtenerRolActual(): String {

        return try {

            val userId = SupabaseClient.client.auth
                .currentUserOrNull()?.id ?: return "cliente"

            val resultado = SupabaseClient.client
                .postgrest["Usuarios"]
                .select {

                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UsuarioData>()

            resultado.firstOrNull()?.rol ?: "cliente"

        } catch (e: Exception) {

            Log.e("DEBUG_QUERY", "Error obtenerRolActual: ${e.message}")
            "cliente"
        }
    }

    suspend fun actualizarPerfil(
        nombres: String,
        apellidos: String,
        correo: String,
        fotoUrl: String? = null
    ) {

        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return

        try {

            val datos = buildJsonObject {

                put("nombre", nombres)
                put("apellidos", apellidos)
                put("correo", correo)

                if (fotoUrl != null) {
                    put("foto_url", fotoUrl)
                }
            }

            SupabaseClient.client
                .postgrest["Usuarios"]
                .update(datos) {

                    filter {
                        eq("id", userId)
                    }
                }

            Log.d("DEBUG_QUERY", "Perfil actualizado")

        } catch (e: Exception) {

            Log.e("DEBUG_QUERY", "Error actualizarPerfil: ${e.message}")
        }
    }

    suspend fun subirFotoPerfil(
        contexto: Context,
        uri: Uri
    ): String {

        val userId = SupabaseClient.client.auth
            .currentUserOrNull()?.id ?: return ""

        Log.d("DEBUG_FOTO", "Uri scheme: ${uri.scheme}")
        Log.d("DEBUG_FOTO", "Uri path: ${uri.path}")

        val bytes = if (uri.scheme == "content") {

            contexto.contentResolver
                .openInputStream(uri)
                ?.use { it.readBytes() }

        } else {

            java.io.File(uri.path!!).readBytes()
        } ?: return ""

        Log.d("DEBUG_FOTO", "Bytes leídos: ${bytes.size}")

        val rutaArchivo = "perfil_$userId.jpg"

        try {

            SupabaseClient.client
                .storage["avatars"]
                .upload(
                    path = rutaArchivo,
                    data = bytes,
                    options = {
                        upsert = true
                    }
                )

            val url = SupabaseClient.client
                .storage["avatars"]
                .publicUrl(rutaArchivo)

            Log.d("DEBUG_FOTO", "URL generada: $url")

            return url

        } catch (e: Exception) {

            Log.e("DEBUG_FOTO", "Error subirFotoPerfil: ${e.message}")
            return ""
        }
    }
}