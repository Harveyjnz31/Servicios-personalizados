package com.example.servihub.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.userProfile.observe(this) { profile ->
            if (profile != null) {
                binding.tvWelcome.text = getString(R.string.welcome_back, profile.fullName)
                binding.btnViewProfile.visibility = View.VISIBLE
                binding.btnStart.text = getString(R.string.btn_edit_profile)
            } else {
                binding.tvWelcome.text = getString(R.string.welcome_title)
                binding.btnViewProfile.visibility = View.GONE
                binding.btnStart.text = getString(R.string.btn_start_registration)
            }
        }
    }

    private fun setupListeners() {
        binding.btnStart.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.btnViewProfile.setOnClickListener {
            startActivity(Intent(this, ProfileDetailsActivity::class.java))
        }
    }
}