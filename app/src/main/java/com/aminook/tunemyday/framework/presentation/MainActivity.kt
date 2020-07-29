package com.aminook.tunemyday.framework.presentation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentFactory
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.aminook.tunemyday.R
import com.aminook.tunemyday.framework.presentation.common.AppFragmentFactory
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var appFragmentFactory: FragmentFactory

    lateinit var navHostFragment:NavHostFragment
    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.fragmentFactory=appFragmentFactory
        setupNavigation()
    }

    private fun setupNavigation(){
        navHostFragment=supportFragmentManager.findFragmentById(R.id.main_nav_host) as NavHostFragment
        navController=navHostFragment.navController
        bottom_navigation.setupWithNavController(navController)
    }
}