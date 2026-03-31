package com.example.taller_2.activities.ui.main.productos

data class Producto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imageRes: Int
)

data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String
)
