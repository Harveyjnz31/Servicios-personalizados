package com.example.servihub.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityProfileDetailsBinding
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private val viewModel: WelcomeViewModel by viewModels {
        ViewModelFactory(UserRepository(AppDatabase.getDatabase(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        
        binding.btnEdit.setOnClickListener {
            // Future implementation for editing profile
        }
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                binding.tvName.text = it.fullName
                binding.tvEmail.text = it.email
                binding.tvPhone.text = it.phone
                binding.tvAge.text = getString(R.string.years_suffix, it.age.toString())
                binding.tvCity.text = it.city
                binding.tvAddress.text = it.address
                binding.rbRating.rating = it.rating
                
                val isProfessional = it.userRole == "PROFESSIONAL"
                binding.tvRole.text = if (isProfessional) getString(R.string.role_professional) else getString(R.string.role_client)
                
                if (isProfessional) {
                    binding.llProfessionalDetails.visibility = View.VISIBLE
                    binding.tvExperience.text = getString(R.string.years_suffix, it.experienceYears.toString())
                    binding.tvSpecialty.text = it.serviceType
                } else {
                    binding.llProfessionalDetails.visibility = View.GONE
                }
            }
        }
    }
}
