package com.example.servihub.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityWelcomeBinding
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var viewModel: WelcomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Force background color to prevent white screen issues
        binding.root.setBackgroundColor(getColor(R.color.background))

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setupObservers()
        setupListeners()
        setupNavigationDrawer()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            val headerView = binding.navView.getHeaderView(0)
            val headerName = headerView.findViewById<TextView>(R.id.nav_header_name)
            val headerEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
            val headerImage = headerView.findViewById<ImageView>(R.id.nav_header_imageView)

            val switchMenuItem = binding.navView.menu.findItem(R.id.nav_switch_role)

            if (profile != null) {
                val roleText = if (profile.userRole == "PROFESSIONAL") "Profesional" else "Cliente"
                binding.tvWelcome.text = getString(R.string.welcome_back, profile.fullName)
                binding.tvSubtitle.text = getString(R.string.mode_prefix, roleText)
                
                binding.llLoggedIn.visibility = View.VISIBLE
                binding.llGuest.visibility = View.GONE
                
                headerName.text = profile.fullName
                headerEmail.text = "${profile.email} - $roleText"
                headerImage.setImageResource(R.drawable.ic_launcher_foreground)

                switchMenuItem?.isVisible = true
                val nextRole = if (profile.userRole == "CLIENT") "Profesional" else "Cliente"
                switchMenuItem?.title = getString(R.string.menu_switch_role, nextRole)
            } else {
                binding.tvWelcome.text = getString(R.string.welcome_title)
                binding.tvSubtitle.text = getString(R.string.welcome_subtitle)
                
                binding.llLoggedIn.visibility = View.GONE
                binding.llGuest.visibility = View.VISIBLE
                
                headerName.text = getString(R.string.nav_header_title)
                headerEmail.text = getString(R.string.nav_header_subtitle)
                headerImage.setImageResource(android.R.drawable.sym_def_app_icon)
                
                switchMenuItem?.isVisible = false
            }
        }
    }

    private fun setupListeners() {
        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            showLoginDialog()
        }
        binding.btnViewProfile.setOnClickListener {
            startActivity(Intent(this, ProfileDetailsActivity::class.java))
        }
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun showLoginDialog() {
        val dialogBinding = com.example.servihub.databinding.DialogLoginBinding.inflate(layoutInflater)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSubmitLogin.setOnClickListener {
            val email = dialogBinding.etLoginEmail.text.toString()
            if (email.isNotBlank()) {
                viewModel.login(email) { success ->
                    runOnUiThread {
                        if (success) {
                            showToast(getString(R.string.login_success))
                            dialog.dismiss()
                        } else {
                            showToast(getString(R.string.error_invalid_email))
                        }
                    }
                }
            } else {
                showToast(getString(R.string.error_empty_email))
            }
        }
        dialog.show()
    }

    private fun setupNavigationDrawer() {
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_history -> showToast(getString(R.string.menu_history))
                R.id.nav_notifications -> showToast(getString(R.string.menu_notifications))
                R.id.nav_city -> showToast(getString(R.string.menu_city))
                R.id.nav_settings -> showToast(getString(R.string.menu_settings))
                R.id.nav_support -> showToast(getString(R.string.menu_support))
                R.id.nav_help -> showToast(getString(R.string.menu_help))
                R.id.nav_share -> shareApp()
                R.id.nav_logout -> viewModel.logout()
                R.id.nav_switch_role -> switchRole()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun switchRole() {
        val currentProfile = viewModel.userProfile.value
        currentProfile?.let {
            val newRole = if (it.userRole == "CLIENT") "PROFESSIONAL" else "CLIENT"
            val updatedProfile = it.copy(userRole = newRole)
            // We need a method in ViewModel to update the profile
            viewModel.updateProfile(updatedProfile)
            showToast("Cambiado a ${if (newRole == "CLIENT") "Cliente" else "Profesional"}")
        }
    }

    private fun shareApp() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "¡Echa un vistazo a ServiHub! La mejor plataforma para profesionales: https://play.google.com/store/apps/details?id=com.example.servihub")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}