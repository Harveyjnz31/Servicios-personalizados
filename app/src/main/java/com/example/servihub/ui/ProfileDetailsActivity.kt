package com.example.servihub.ui

import android.os.Bundle
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
                binding.tvExperience.text = getString(R.string.years_suffix, it.experienceYears.toString())
                binding.tvSpecialty.text = it.serviceType
            }
        }
    }
}
