package com.example.taller_2.activities.ui.main

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.taller_2.R
import com.example.taller_2.activities.ui.main.admin.AdminFragment
import com.example.taller_2.activities.ui.main.admin.UsuariosFragment
import com.example.taller_2.activities.ui.main.perfil.PerfilFragment
import com.example.taller_2.activities.ui.main.productos.CarritoFragment
import com.example.taller_2.activities.ui.main.productos.CatalogoFragment
import com.example.taller_2.activities.ui.main.productos.FavoritosFragment
import com.example.taller_2.activities.ui.main.productos.HomeFragment

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
                R.id.nav_perfil -> cargarFragment(PerfilFragment())
                R.id.nav_usua -> cargarFragment(UsuariosFragment())
                R.id.nav_admin -> cargarFragment(AdminFragment())
            }
            drawerLayout.closeDrawers()
            true
            }

    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()


    }
}