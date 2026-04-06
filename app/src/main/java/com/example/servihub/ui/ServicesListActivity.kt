package com.example.servihub.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityServicesListBinding
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class ServicesListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityServicesListBinding
    private lateinit var viewModel: WelcomeViewModel
    private lateinit var adapter: ProfessionalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServicesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Estilo formal para las barras del sistema
        window.statusBarColor = Color.parseColor("#FFFFFF")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#F1F5F9")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or 
                                                 View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = ProfessionalAdapter(emptyList())
        binding.rvProfessionals.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvProfessionals.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allProfessionals.observe(this) { professionals ->
            // Siempre llamamos a filterList para que se muestren al menos los datos simulados
            // si la base de datos está vacía.
            filterList(professionals ?: emptyList())
        }

        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            filterList(viewModel.allProfessionals.value ?: emptyList())
        }
    }

    private fun filterList(allReal: List<com.example.servihub.model.UserProfile>) {
        val selectedChip = binding.chipGroup.checkedChipId
        
        val filteredReal = when (selectedChip) {
            R.id.chipElectric -> allReal.filter { it.serviceType.contains("Elec", ignoreCase = true) }
            R.id.chipPlumbing -> allReal.filter { it.serviceType.contains("Plom", ignoreCase = true) || it.serviceType.contains("Font", ignoreCase = true) }
            R.id.chipCleaning -> allReal.filter { it.serviceType.contains("Limp", ignoreCase = true) }
            R.id.chipCivil -> allReal.filter { it.serviceType.contains("Obra", ignoreCase = true) || it.serviceType.contains("Alba", ignoreCase = true) }
            R.id.chipWelding -> allReal.filter { it.serviceType.contains("Sold", ignoreCase = true) }
            R.id.chipPainting -> allReal.filter { it.serviceType.contains("Pint", ignoreCase = true) }
            R.id.chipMechanic -> allReal.filter { it.serviceType.contains("Mec", ignoreCase = true) }
            R.id.chipCarpentry -> allReal.filter { it.serviceType.contains("Carp", ignoreCase = true) }
            else -> allReal
        }

        // Combine with mock data if real data is scarce
        val mockData = getMockProfessionalsForCategory(selectedChip)
        val combined = (filteredReal + mockData).distinctBy { it.fullName }
        
        adapter.updateList(combined)
        
        if (combined.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvProfessionals.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvProfessionals.visibility = View.VISIBLE
        }
    }

    private fun getMockProfessionalsForCategory(chipId: Int): List<com.example.servihub.model.UserProfile> {
        val mocks = listOf(
            com.example.servihub.model.UserProfile(fullName = "Carlos Ruiz", email = "carlos@mock.com", serviceType = "Electricista Certificado", phone = "555-0101", address = "", city = "Bogotá", age = 30, experienceYears = 5),
            com.example.servihub.model.UserProfile(fullName = "Ana Marta", email = "ana@mock.com", serviceType = "Limpieza de Hogar", phone = "555-0102", address = "", city = "Bogotá", age = 28, experienceYears = 3),
            com.example.servihub.model.UserProfile(fullName = "Roberto Gómez", email = "roberto@mock.com", serviceType = "Plomería 24/7", phone = "555-0103", address = "", city = "Medellín", age = 45, experienceYears = 20),
            com.example.servihub.model.UserProfile(fullName = "Marcos Peña", email = "marcos@mock.com", serviceType = "Obra Civil / Albañil", phone = "555-0104", address = "", city = "Cali", age = 35, experienceYears = 10),
            com.example.servihub.model.UserProfile(fullName = "Julia Santos", email = "julia@mock.com", serviceType = "Soldadura Industrial", phone = "555-0105", address = "", city = "Bogotá", age = 32, experienceYears = 8),
            com.example.servihub.model.UserProfile(fullName = "Pedro Alva", email = "pedro@mock.com", serviceType = "Pintura de Exteriores", phone = "555-0106", address = "", city = "Cartagena", age = 29, experienceYears = 6),
            com.example.servihub.model.UserProfile(fullName = "Luis Rivas", email = "luis@mock.com", serviceType = "Mecánica Automotriz", phone = "555-0107", address = "", city = "Barranquilla", age = 40, experienceYears = 15),
            com.example.servihub.model.UserProfile(fullName = "Sonia Tello", email = "sonia@mock.com", serviceType = "Carpintería Fina", phone = "555-0108", address = "", city = "Bogotá", age = 33, experienceYears = 7)
        )

        return when (chipId) {
            R.id.chipElectric -> mocks.filter { it.serviceType.contains("Elec", ignoreCase = true) }
            R.id.chipPlumbing -> mocks.filter { it.serviceType.contains("Plom", ignoreCase = true) }
            R.id.chipCleaning -> mocks.filter { it.serviceType.contains("Limp", ignoreCase = true) }
            R.id.chipCivil -> mocks.filter { it.serviceType.contains("Obra", ignoreCase = true) }
            R.id.chipWelding -> mocks.filter { it.serviceType.contains("Sold", ignoreCase = true) }
            R.id.chipPainting -> mocks.filter { it.serviceType.contains("Pint", ignoreCase = true) }
            R.id.chipMechanic -> mocks.filter { it.serviceType.contains("Mec", ignoreCase = true) }
            R.id.chipCarpentry -> mocks.filter { it.serviceType.contains("Carp", ignoreCase = true) }
            else -> mocks
        }
    }
}
