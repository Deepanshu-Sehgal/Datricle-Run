package com.datricle.datriclerun.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.datricle.datriclerun.R
import com.datricle.datriclerun.databinding.ActivityMainBinding
import com.datricle.datriclerun.others.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setSupportActionBar(binding.toolbar)

        navHostFragment = findNavController(R.id.nav_frag)
        binding.bottomNavigation.setupWithNavController(navHostFragment)
        binding.bottomNavigation.setOnNavigationItemReselectedListener { /* NO-OP */ }

        navigateToTrackingFragmentIfNeeded(intent)

        navHostFragment
            .addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.setupFragment-> {
                        binding.bottomNavigation.visibility = View.GONE
                        binding.appBarLayout.visibility = View.GONE
                    }
                    R.id.trackingFragment -> {
                        binding.bottomNavigation.visibility = View.GONE
                        binding.appBarLayout.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.bottomNavigation.visibility = View.VISIBLE
                        binding.appBarLayout.visibility = View.VISIBLE
                    }
                }
            }
    }


    //Checks if we launched the activity from the notification
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostFragment.navigate(R.id.action_global_trackingFragment)
        }
    }
    /*val navController: NavController =
        Navigation.findNavController(this, R.id.nav_frag)

    val bottomNavigationView =
        findViewById<BottomNavigationView>(R.id.bottomNavigation)

    NavigationUI.setupWithNavController(bottomNavigationView, navController)


    navController.addOnDestinationChangedListener { _, destination, _ ->

        when (destination.id) {
            R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment2 -> {
                bottomNavigationView.visibility = View.VISIBLE
                binding.tvToolbarTitle.visibility = View.VISIBLE
            }
            else -> {
                bottomNavigationView.visibility =
                    View.GONE
                binding.tvToolbarTitle.visibility = View.GONE
            }

        }


    }

    //binding.bottomNavigationView.setupWithNavController(binding.navHostFragmentContainer)
    *//*val runFragment = RunFragment()
    val statisticsFragment = StatisticsFragment()
    val settingsFragment = SettingsFragment()

    setCurrentFragment(runFragment)

    binding.bottomNavigationView.onTabSelected = {
        when (it.id) {
            R.id.runFragment -> setCurrentFragment(runFragment)
            R.id.statisticsFragment -> setCurrentFragment(statisticsFragment)
            R.id.settingsFragment -> setCurrentFragment(settingsFragment)
        }
    }
}

private fun setCurrentFragment(fragment: Fragment) =
    supportFragmentManager.beginTransaction().apply {
        replace(R.id.flFragment, fragment)
        commit()
    }
*//*
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
        }
    }*/
}

