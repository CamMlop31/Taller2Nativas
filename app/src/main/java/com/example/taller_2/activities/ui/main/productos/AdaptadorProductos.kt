package com.example.taller_2.activities.ui.main.productos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.taller_2.R

class AdaptadorProductos(private val productos: List<Producto>) :
    RecyclerView.Adapter<AdaptadorProductos.ProductoViewHolder>() {

    inner class ProductoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.imagen_producto)
        val nombre: TextView = itemView.findViewById(R.id.nombre_producto)
        val precio: TextView = itemView.findViewById(R.id.precio_producto)

        val descripcion: TextView = itemView.findViewById(R.id.descripcion_producto)
        val btnAgregar: Button = itemView.findViewById(R.id.boton_agregar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(View)
    }

    override fun getItemCount(): Int = productos.size

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.image.setImageResource(producto.imageRes)
        holder.nombre.text = producto.nombre
        holder.precio.text = "$${producto.precio}"
        holder.descripcion.text = producto.descripcion
        holder.btnAgregar.setOnClickListener {

        }
    }

}