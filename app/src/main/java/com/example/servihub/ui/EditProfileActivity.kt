package com.example.servihub.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityEditProfileBinding
import com.example.servihub.model.UserProfile
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val viewModel: WelcomeViewModel by viewModels {
        val db = AppDatabase.getDatabase(this)
        ViewModelFactory(UserRepository(db.userDao(), db.reviewDao(), db.workRequestDao(), db.favoriteDao(), db.chatDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
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
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        setupObservers()
        
        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                binding.etFullName.setText(it.fullName)
                binding.etPhone.setText(it.phone)
                binding.etAge.setText(it.age.toString())
                binding.etCity.setText(it.city)
                binding.etAddress.setText(it.address)
                
                if (it.userRole == "PROFESSIONAL") {
                    binding.llProfessionalFields.visibility = View.VISIBLE
                    binding.etExperience.setText(it.experienceYears.toString())
                    binding.etSpecialty.setText(it.serviceType)
                } else {
                    binding.llProfessionalFields.visibility = View.GONE
                }
            }
        }
    }

    private fun saveProfile() {
        val currentProfile = viewModel.userProfile.value ?: return
        
        val updatedProfile = currentProfile.copy(
            fullName = binding.etFullName.text.toString(),
            phone = binding.etPhone.text.toString(),
            age = binding.etAge.text.toString().toIntOrNull() ?: currentProfile.age,
            city = binding.etCity.text.toString(),
            address = binding.etAddress.text.toString(),
            experienceYears = binding.etExperience.text.toString().toIntOrNull() ?: currentProfile.experienceYears,
            serviceType = binding.etSpecialty.text.toString()
        )
        
        viewModel.updateProfile(updatedProfile)
        Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()
        finish()
    }
}
