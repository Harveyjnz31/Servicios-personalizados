package com.example.servihub.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
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
        
        // Triple security: Hide menu button immediately on startup
        binding.btnMenu.visibility = View.GONE
        
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

            // Dynamic visibility of menu groups
            binding.navView.menu.setGroupVisible(R.id.group_user_actions, profile != null)
            binding.navView.menu.findItem(R.id.nav_more_section).isVisible = profile != null

            if (profile != null) {
                // Enabled drawer for logged users
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.btnMenu.visibility = View.VISIBLE

                val roleText = if (profile.userRole == "PROFESSIONAL") "Profesional" else "Cliente"
                binding.tvWelcome.text = getString(R.string.welcome_back, profile.fullName)
                binding.tvSubtitle.text = getString(R.string.mode_prefix, roleText)
                
                binding.llLoggedIn.visibility = View.VISIBLE
                binding.llGuest.visibility = View.GONE

                // Show services list for clients, show icon for professionals
                if (profile.userRole == "CLIENT") {
                    binding.llServices.visibility = View.VISIBLE
                    binding.ivMainIcon.visibility = View.GONE
                    setupExampleProfessionals()
                } else {
                    binding.llServices.visibility = View.GONE
                    binding.ivMainIcon.visibility = View.VISIBLE
                }
                
                headerName.text = profile.fullName
                headerEmail.text = "${profile.email} - $roleText"
                
                // Assign role-based avatar
                if (profile.userRole == "PROFESSIONAL") {
                    headerImage.setImageResource(android.R.drawable.ic_menu_manage) // Tool icon for pro
                } else {
                    headerImage.setImageResource(android.R.drawable.ic_menu_myplaces) // House/Place for client
                }

                val switchMenuItem = binding.navView.menu.findItem(R.id.nav_switch_role)
                val nextRole = if (profile.userRole == "CLIENT") "Profesional" else "Cliente"
                switchMenuItem?.title = getString(R.string.menu_switch_role, nextRole)
            } else {
                // Disabled drawer for guest users
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                binding.btnMenu.visibility = View.GONE
                binding.llServices.visibility = View.GONE
                binding.ivMainIcon.visibility = View.VISIBLE

                binding.tvWelcome.text = getString(R.string.welcome_title)
                binding.tvSubtitle.text = getString(R.string.welcome_subtitle)
                
                binding.llLoggedIn.visibility = View.GONE
                binding.llGuest.visibility = View.VISIBLE
                
                headerName.text = getString(R.string.nav_header_title)
                headerEmail.text = getString(R.string.nav_header_subtitle)
                headerImage.setImageResource(android.R.drawable.sym_def_app_icon)
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
        dialogBinding.btnForgotPassword.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.btn_forgot_password))
                .setMessage(getString(R.string.forgot_password_message))
                .setPositiveButton("OK", null)
                .show()
        }
        dialog.show()
    }

    private fun setupExampleProfessionals() {
        binding.llProfessionalList.removeAllViews()
        val inflater = LayoutInflater.from(this)
        
        val examples = listOf(
            Pair("Carlos Ruiz", "Electricista Certificado"),
            Pair("Ana Marta", "Limpieza de Hogar"),
            Pair("Roberto Gómez", "Plomería 24/7"),
            Pair("Lucía Fernández", "Jardinería y Paisajismo")
        )

        for (example in examples) {
            val itemView = inflater.inflate(R.layout.item_professional_example, binding.llProfessionalList, false)
            itemView.findViewById<TextView>(R.id.tvExampleName).text = example.first
            itemView.findViewById<TextView>(R.id.tvExampleService).text = example.second
            binding.llProfessionalList.addView(itemView)
        }
    }

    private fun setupNavigationDrawer() {
        // Initially lock the drawer and hide menu button
        binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.btnMenu.visibility = View.GONE

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
