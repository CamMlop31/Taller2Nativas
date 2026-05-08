package com.example.taller_2.activities.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.taller_2.R
import com.example.taller_2.activities.SupabaseClient
import com.example.taller_2.activities.ui.auth.Login
import com.example.taller_2.activities.ui.main.admin.AdminFragment
import com.example.taller_2.activities.ui.main.admin.UsuariosFragment
import com.example.taller_2.activities.ui.main.perfil.PerfilFragment
import com.example.taller_2.activities.ui.main.perfil.editarPerfilFragment
import com.example.taller_2.activities.ui.main.productos.CarritoFragment
import com.example.taller_2.activities.ui.main.productos.CatalogoFragment
import com.example.taller_2.activities.ui.main.productos.FavoritosFragment
import com.example.taller_2.activities.ui.main.productos.HomeFragment
import com.example.taller_2.data.UsuarioRepository
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        //Configuraciòn del menu hamburguesa

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        drawerLayout = findViewById<DrawerLayout>(R.id.drawerlayout)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)

        cargarFragment(HomeFragment())
        bottomNav.selectedItemId = R.id.nav_home

        //configurar menu por rol

        configurarMenuPorRol(navView.menu)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cargarFragment(HomeFragment())
                R.id.nav_catalogo -> cargarFragment(CatalogoFragment())
                R.id.nav_carrito -> cargarFragment(CarritoFragment())
                R.id.nav_favoritos -> cargarFragment(FavoritosFragment())
                R.id.nav_perfil -> cargarFragment(PerfilFragment())
            }
            true
            }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> cargarFragment(HomeFragment())
                R.id.nav_catalogo -> cargarFragment(CatalogoFragment())
                R.id.nav_carrito -> cargarFragment(CarritoFragment())
                R.id.nav_favoritos -> cargarFragment(FavoritosFragment())
                R.id.nav_editarPerfil -> cargarFragment(editarPerfilFragment())
                R.id.nav_perfil -> cargarFragment(PerfilFragment())
                R.id.nav_usua -> cargarFragment(UsuariosFragment())
                R.id.nav_admin -> cargarFragment(AdminFragment())
                R.id.nav_logout -> cerrarSesion()
            }
            drawerLayout.closeDrawers()
            true
            }
    }

    private fun configurarMenuPorRol(menu: Menu) {

        lifecycleScope.launch {

            val user = SupabaseClient.client.auth.currentUserOrNull()
            if (user == null) return@launch

            val rol = UsuarioRepository.obtenerRolActual()
            android.util.Log.d("DEBUG_ROL", "Rol del usuario: $rol")


            //  MENÚS COMUNES

            menu.findItem(R.id.nav_home)?.isVisible = true
            menu.findItem(R.id.nav_catalogo)?.isVisible = true
            menu.findItem(R.id.nav_carrito)?.isVisible = true
            menu.findItem(R.id.nav_favoritos)?.isVisible = true
            menu.findItem(R.id.nav_perfil)?.isVisible = true
            menu.findItem(R.id.nav_editarPerfil)?.isVisible = true
            menu.findItem(R.id.nav_logout)?.isVisible = true


            // MENÚS POR ROL

            when (rol.lowercase()) {

                "admin" -> {
                    menu.findItem(R.id.nav_admin)?.isVisible = true
                    menu.findItem(R.id.nav_usua)?.isVisible = true
                }

                "anfitrion" -> {
                    menu.findItem(R.id.nav_admin)?.isVisible = true
                    menu.findItem(R.id.nav_usua)?.isVisible = false
                }

                else -> {
                    // cliente
                    menu.findItem(R.id.nav_admin)?.isVisible = false
                    menu.findItem(R.id.nav_usua)?.isVisible = false
                }
            }
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

    }

    private fun cerrarSesion() {
        lifecycleScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@MainActivity, Login::class.java))
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error al cerrar sesión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

