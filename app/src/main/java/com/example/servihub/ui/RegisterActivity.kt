package com.example.servihub.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityRegisterBinding
import com.example.servihub.viewmodel.RegisterViewModel
import com.example.servihub.viewmodel.ViewModelFactory

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao(), database.workRequestDao(), database.favoriteDao(), database.chatDao())
        val factory = ViewModelFactory(repository)
        
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.registrationSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Perfil Guardado con Éxito", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupListeners() {
        binding.rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbProfessional) {
                binding.llProfessionalFields.visibility = View.VISIBLE
            } else {
                binding.llProfessionalFields.visibility = View.GONE
            }
        }

        binding.btnSave.setOnClickListener {
            val role = if (binding.rbProfessional.isChecked) "PROFESSIONAL" else "CLIENT"
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val phone = binding.etPhone.text.toString()
            val age = binding.etAge.text.toString()
            val city = binding.etCity.text.toString()
            val address = binding.etAddress.text.toString()
            val service = binding.etService.text.toString()
            val exp = binding.etExp.text.toString()

            viewModel.registerUser(role, name, email, password, phone, age, city, address, service, exp)
        }
    }
}