package com.example.taller_2.activities

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    val client = createSupabaseClient(
        supabaseUrl = "https://liyzusobaxrzieglxjkr.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImxpeXp1c29iYXhyemllZ2x4amtyIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzU1MTkxNzIsImV4cCI6MjA5MTA5NTE3Mn0.JIX9yZX37GImh90nBT2ipydtHLvAHRE8eUf_LjK2Y8E"
    ){
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}