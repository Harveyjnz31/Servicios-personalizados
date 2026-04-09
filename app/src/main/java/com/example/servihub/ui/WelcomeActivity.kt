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
import com.example.servihub.model.WorkRequest
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
        
        binding.btnMenu.visibility = View.GONE
        binding.root.setBackgroundResource(R.drawable.bg_gradient_guest)

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao(), database.workRequestDao(), database.favoriteDao(), database.chatDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setupObservers()
        setupListeners()
        setupNavigationDrawer()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            updateUI(profile)
        }

        viewModel.allWorkRequests.observe(this) { requests ->
            val profile = viewModel.userProfile.value
            if (profile?.userRole == "PROFESSIONAL") {
                if (requests.isEmpty()) {
                    seedMockRequests()
                } else {
                    displayRequests(requests)
                }
            }
        }
    }

    private fun updateUI(profile: com.example.servihub.model.UserProfile?) {
        val headerView = binding.navView.getHeaderView(0)
        val headerName = headerView.findViewById<TextView>(R.id.nav_header_name)
        val headerEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        val headerImage = headerView.findViewById<ImageView>(R.id.nav_header_imageView)

        if (profile != null) {
            // MODO LOGUEADO
            binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
            binding.btnMenu.visibility = View.VISIBLE
            binding.bottomNavDivider.visibility = View.VISIBLE
            binding.bottomNavigation.visibility = View.VISIBLE
            binding.cvTopActions.visibility = View.VISIBLE
            binding.llLoggedIn.visibility = View.VISIBLE
            binding.llGuest.visibility = View.GONE
            binding.actionsCard.visibility = View.GONE
            binding.ivMainIcon.visibility = View.GONE
            binding.tvSubtitle.visibility = View.VISIBLE

            val roleText = if (profile.userRole == "PROFESSIONAL") "Profesional" else "Cliente"
            binding.tvRoleStatus.text = "Modo: $roleText"
            binding.tvWelcome.text = getString(R.string.welcome_title)

            if (profile.userRole == "CLIENT") {
                binding.root.setBackgroundResource(R.drawable.bg_gradient_client)
                binding.llServices.visibility = View.VISIBLE
                binding.llRequests.visibility = View.GONE
                setupExampleProfessionals()
                setupRecentReviews()
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_gradient_professional)
                binding.llServices.visibility = View.GONE
                binding.llRequests.visibility = View.VISIBLE
                setupProfessionalSummary()
            }

            headerName.text = profile.fullName
            headerEmail.text = "${profile.email} - $roleText"
            headerImage.setImageResource(if (profile.userRole == "PROFESSIONAL") android.R.drawable.ic_menu_manage else android.R.drawable.ic_menu_myplaces)

            binding.navView.menu.setGroupVisible(R.id.group_user_actions, true)
            binding.navView.menu.findItem(R.id.nav_more_section).isVisible = true
            val switchMenuItem = binding.navView.menu.findItem(R.id.nav_switch_role)
            val nextRole = if (profile.userRole == "CLIENT") "Profesional" else "Cliente"
            switchMenuItem?.title = getString(R.string.menu_switch_role, nextRole)

            binding.btnSwitchRoleTop.setOnClickListener { switchRole() }
        } else {
            // MODO GUEST (Logout)
            binding.root.setBackgroundResource(R.drawable.bg_gradient_guest)
            binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            
            // Ocultar TODO lo relacionado a usuario
            binding.btnMenu.visibility = View.GONE
            binding.llServices.visibility = View.GONE
            binding.llRequests.visibility = View.GONE
            binding.bottomNavigation.visibility = View.GONE
            binding.cvTopActions.visibility = View.GONE
            binding.llLoggedIn.visibility = View.GONE
            binding.bottomNavDivider.visibility = View.GONE
            
            // Limpiar contenedores
            binding.llProfessionalList.removeAllViews()
            binding.llRecentReviews.removeAllViews()
            binding.llRequestsList.removeAllViews()
            binding.llProfessionalSummary.removeAllViews()

            // Mostrar vista de invitado limpia
            binding.ivMainIcon.visibility = View.VISIBLE
            binding.tvWelcome.text = getString(R.string.welcome_title)
            binding.tvSubtitle.visibility = View.VISIBLE
            binding.tvSubtitle.text = getString(R.string.welcome_subtitle)
            binding.llGuest.visibility = View.VISIBLE
            binding.actionsCard.visibility = View.VISIBLE
            
            headerName.text = getString(R.string.nav_header_title)
            headerEmail.text = getString(R.string.nav_header_subtitle)
            headerImage.setImageResource(android.R.drawable.sym_def_app_icon)
            
            binding.navView.menu.setGroupVisible(R.id.group_user_actions, false)
            binding.navView.menu.findItem(R.id.nav_more_section).isVisible = false
        }
    }

    private fun displayRequests(requests: List<WorkRequest>) {
        binding.llRequestsList.removeAllViews()
        val inflater = LayoutInflater.from(this)
        for (request in requests) {
            val itemView = inflater.inflate(R.layout.item_request_example, binding.llRequestsList, false)
            itemView.findViewById<TextView>(R.id.tvRequestTitle).text = request.title
            itemView.findViewById<TextView>(R.id.tvRequestCategory).text = request.specialty
            itemView.findViewById<TextView>(R.id.tvRequestDescription).text = request.description
            itemView.findViewById<TextView>(R.id.tvRequestLocation).text = request.location
            binding.llRequestsList.addView(itemView)
        }
    }

    private fun seedMockRequests() {
        val mocks = listOf(
            WorkRequest(title = "Reparación Cortocircuito", specialty = "ELECTRICIDAD", description = "Falla en tablero principal.", location = "Norte", clientId = 0, clientName = "Sistema"),
            WorkRequest(title = "Fuga de Agua", specialty = "PLOMERÍA", description = "Goteo constante en cocina.", location = "Centro", clientId = 0, clientName = "Sistema"),
            WorkRequest(title = "Limpieza de Oficina", specialty = "LIMPIEZA", description = "Limpieza tras mudanza.", location = "Sur", clientId = 0, clientName = "Sistema")
        )
        for (m in mocks) viewModel.insertWorkRequest(m)
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
        binding.btnViewAllServices.setOnClickListener {
            startActivity(Intent(this, ServicesListActivity::class.java))
        }
        binding.btnViewAllRequests.setOnClickListener {
            startActivity(Intent(this, RequestsListActivity::class.java))
        }
        binding.btnPostRequest.setOnClickListener {
            startActivity(Intent(this, PostRequestActivity::class.java))
        }
        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> startActivity(Intent(this, ServicesListActivity::class.java))
                R.id.nav_favorites -> showToast("Próximamente")
                R.id.nav_contacts -> showToast("Próximamente")
                R.id.nav_profile -> startActivity(Intent(this, ProfileDetailsActivity::class.java))
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
            val password = dialogBinding.etLoginPassword.text.toString()
            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password) { success ->
                    runOnUiThread {
                        if (success) {
                            showToast(getString(R.string.login_success))
                            dialog.dismiss()
                        } else {
                            showToast("Error de credenciales")
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun setupExampleProfessionals() {
        binding.llProfessionalList.removeAllViews()
        val inflater = LayoutInflater.from(this)
        val examples = listOf(
            Triple("Carlos Ruiz", "Electricista", "4.9"),
            Triple("Ana Marta", "Limpieza", "4.8")
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
            Triple("María G.", "Excelente limpieza ✨", "Hace 5m"),
            Triple("Juan P.", "Gran profesional eléctrico.", "Hace 15m")
        )
        for (review in reviews) {
            val itemView = inflater.inflate(R.layout.item_review_notification, binding.llRecentReviews, false)
            itemView.findViewById<TextView>(R.id.tvReviewAuthor).text = review.first
            itemView.findViewById<TextView>(R.id.tvReviewText).text = review.second
            itemView.findViewById<TextView>(R.id.tvReviewTime).text = review.third
            binding.llRecentReviews.addView(itemView)
        }
    }

    private fun setupProfessionalSummary() {
        binding.llProfessionalSummary.removeAllViews()
        val inflater = LayoutInflater.from(this)
        val summaries = listOf(
            Triple("Ganancias", "$1,250", "Este mes"),
            Triple("Completados", "14", "Últimos 30 días")
        )
        for (summary in summaries) {
            val itemView = inflater.inflate(R.layout.item_review_notification, binding.llProfessionalSummary, false)
            itemView.findViewById<TextView>(R.id.tvReviewAuthor).text = summary.first
            itemView.findViewById<TextView>(R.id.tvReviewText).text = summary.second
            itemView.findViewById<TextView>(R.id.tvReviewTime).text = summary.third
            binding.llProfessionalSummary.addView(itemView)
        }
    }

    private fun setupNavigationDrawer() {
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#F1F5F9")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
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
            viewModel.updateProfile(it.copy(userRole = newRole))
        }
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
