package com.example.taller_2.activities.ui.main.productos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.taller_2.R


class CatalogoFragment : Fragment() {
    private val ListaProductos = listOf(
        Producto(1, "Consulta Primera Vez",
            "Consulta con un especialista, en MindSoulHeart tenemos el gusto de atenderte. Pregunta por nuestras consultas de primera vez",
            60.999, R.drawable.salud_integral),
        Producto(1, "Consulta Salud Mental Primera Vez",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos la salud mental de nuestros pacientes. Pregunta por nuestras consultas de primera vez",
            80.999, R.drawable.psicologia_vez),
        Producto(1, "Consulta Salud Mental Seguimiento",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos la salud mental de nuestros pacientes. Agenda tus sesiones de seguimiento con tu profesional de confianza",
            80.999, R.drawable.seguimiento_psicolo),
        Producto(1, "Consulta Salud Financiera Primera Vez",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos la salud Financiera de nuestros usuarios, una buena economia brinda estabilidad. Pregunta por nuestras consultas de primera vez",
            80.999, R.drawable.salud_financiera),
        Producto(1, "Consulta Salud Financiera Seguimiento",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos la salud financiera de nuestros usuarios. Agenda tus sesiones de seguimiento y manten tus finanzas bajo control",
            80.999, R.drawable.seguimiento_salud_financiera),
        Producto(1, "Consulta Nutricion y Buen Comer",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos los habitos alimenticios de nuestros usuarios. Pregunta por nuestras consultas de primera vez",
            80.999, R.drawable.nutricion_primera),
        Producto(1, "Consulta Fisioterapia",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos los tu salud corporal, articular, motriz y osea. Pregunta por nuestras consultas de primera vez",
            80.999, R.drawable.fisioterapia),
        Producto(1, "Consulta Manejo del estres",
            "Consulta con tu especialista de confianza, en MindSoulHeart priorizamos tu bienestar y te brindamos herramientas para el manejo del estres de nuestros usuarios. Pregunta por nuestras consultas de primera vez",
            80.999, R.drawable.manejo_del_estres),


        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val RecyclerView = view.findViewById<RecyclerView>(R.id.recycler_productos)
        RecyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        RecyclerView.adapter = AdaptadorProductos(ListaProductos)
        return view

    }



}