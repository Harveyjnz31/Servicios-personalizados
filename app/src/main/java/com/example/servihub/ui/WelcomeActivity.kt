package com.example.servihub.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Color
import android.os.Build
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
    private var isJustRegistered = false

    private val registerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            isJustRegistered = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Triple security: Hide menu button immediately on startup
        binding.btnMenu.visibility = View.GONE
        
        // Force background to prevent white screen issues
        binding.root.setBackgroundResource(R.drawable.bg_gradient_guest)

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
            binding.navView.menu.setGroupVisible(R.id.group_user_actions, true)
            binding.navView.menu.findItem(R.id.nav_more_section).isVisible = true
            
            // Sync bottom divider visibility
            binding.bottomNavDivider.visibility = if (profile != null) View.VISIBLE else View.GONE

            if (profile != null) {
                // Enabled drawer for logged users
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.btnMenu.visibility = View.VISIBLE

                // Set click listeners for profile header elements
                val openProfile = View.OnClickListener {
                    startActivity(Intent(this, ProfileDetailsActivity::class.java))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                }
                headerName.setOnClickListener(openProfile)
                headerEmail.setOnClickListener(openProfile)
                headerImage.setOnClickListener(openProfile)

                val roleText = if (profile.userRole == "PROFESSIONAL") "Profesional" else "Cliente"
                
                val welcomeRes = if (isJustRegistered) R.string.welcome_new else R.string.welcome_back
                binding.tvWelcome.text = getString(welcomeRes, profile.fullName)
                
                binding.tvSubtitle.visibility = View.GONE
                binding.btnSwitchRoleQuick.visibility = View.VISIBLE
                binding.btnSwitchRoleQuick.text = "Modo: $roleText"
                
                binding.llLoggedIn.visibility = View.VISIBLE
                binding.llGuest.visibility = View.GONE
                binding.bottomNavigation.visibility = View.VISIBLE

                // Show services list for clients, show icon for professionals
                if (profile.userRole == "CLIENT") {
                    binding.root.setBackgroundResource(R.drawable.bg_gradient_client)
                    binding.llServices.visibility = View.VISIBLE
                    binding.ivMainIcon.visibility = View.GONE
                    setupExampleProfessionals()
                    setupRecentReviews()
                } else {
                    binding.root.setBackgroundResource(R.drawable.bg_gradient_professional)
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
                // Enabled drawer for guests to see Support/Help
                binding.root.setBackgroundResource(R.drawable.bg_gradient_guest)
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.btnMenu.visibility = View.VISIBLE
                binding.llServices.visibility = View.GONE
                binding.ivMainIcon.visibility = View.VISIBLE

                binding.tvWelcome.text = getString(R.string.welcome_title)
                binding.tvSubtitle.visibility = View.VISIBLE
                binding.tvSubtitle.text = getString(R.string.welcome_subtitle)
                binding.btnSwitchRoleQuick.visibility = View.GONE
                
                binding.llLoggedIn.visibility = View.GONE
                binding.llGuest.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.GONE
                
                headerName.text = getString(R.string.nav_header_title)
                headerEmail.text = getString(R.string.nav_header_subtitle)
                headerImage.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }

    private fun setupListeners() {
        binding.btnStart.setOnClickListener {
            registerLauncher.launch(Intent(this, RegisterActivity::class.java))
        }
        binding.btnLogin.setOnClickListener {
            isJustRegistered = false
            showLoginDialog()
        }
        binding.btnMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        binding.btnSwitchRoleQuick.setOnClickListener {
            switchRole()
        }
        binding.btnViewAllServices.setOnClickListener {
            startActivity(Intent(this, ServicesListActivity::class.java))
        }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> {
                    startActivity(Intent(this, ServicesListActivity::class.java))
                }
                R.id.nav_favorites -> {
                    showToast("Función de Favoritos próximamente")
                }
                R.id.nav_contacts -> {
                    showToast("Bandeja de Contactos próximamente")
                }
                R.id.nav_settings -> {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                    // Enfocar configuración en el drawer si es posible, o abrir actividad dedicada
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileDetailsActivity::class.java))
                }
            }
            true
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
            Triple("Carlos Ruiz", "Electricista Certificado", "4.9"),
            Triple("Ana Marta", "Limpieza de Hogar", "4.8"),
            Triple("Roberto Gómez", "Plomería 24/7", "4.7"),
            Triple("Lucía Fernández", "Jardinería y Paisajismo", "4.9"),
            Triple("Marcos Peña", "Obra Civil / Albañil", "4.6"),
            Triple("Julia Santos", "Soldadura Industrial", "4.8"),
            Triple("Pedro Alva", "Pintura de Exteriores", "4.7"),
            Triple("Luis Rivas", "Mecánica Automotriz", "4.7"),
            Triple("Sonia Tello", "Carpintería Fina", "4.9")
        )

        for (example in examples) {
            val itemView = inflater.inflate(R.layout.item_professional_example, binding.llProfessionalList, false)
            itemView.findViewById<TextView>(R.id.tvExampleName).text = example.first
            itemView.findViewById<TextView>(R.id.tvExampleService).text = example.second
            itemView.findViewById<TextView>(R.id.tvExampleRating).text = example.third
            binding.llProfessionalList.addView(itemView)
        }
    }

    private fun setupRecentReviews() {
        binding.llRecentReviews.removeAllViews()
        val inflater = LayoutInflater.from(this)

        val reviews = listOf(
            Triple("María García", "Excelente servicio de limpieza, Ana fue muy detallista. ✨", "Hace 5m"),
            Triple("Juan Pérez", "Carlos arregló mi tablero eléctrico en minutos. Gran profesional.", "Hace 15m"),
            Triple("Sofía Luna", "El jardín quedó increíble, Lucía tiene un gusto exquisito.", "Hace 1h"),
            Triple("Ricardo M.", "Roberto me salvó con una fuga de agua a medianoche. 10/10", "Hace 2h")
        ).shuffled()

        for (review in reviews) {
            val itemView = inflater.inflate(R.layout.item_review_notification, binding.llRecentReviews, false)
            itemView.findViewById<TextView>(R.id.tvReviewAuthor).text = review.first
            itemView.findViewById<TextView>(R.id.tvReviewText).text = review.second
            itemView.findViewById<TextView>(R.id.tvReviewTime).text = review.third
            binding.llRecentReviews.addView(itemView)
        }
    }

    private fun setupNavigationDrawer() {
        // Show menu button always for professional tool access (Support/Help)
        binding.btnMenu.visibility = View.VISIBLE
        binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
        
        // Estilo formal para las barras del sistema
        window.statusBarColor = Color.parseColor("#FFFFFF")
        
        // Navigation Bar styling with safety check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#EEEEEE") // Gris claro para distinguir botones
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or 
                                                 View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        } else {
            window.navigationBarColor = Color.BLACK
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }

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
