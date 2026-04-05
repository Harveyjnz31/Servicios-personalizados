package com.example.servihub.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
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
        val repository = UserRepository(database.userDao())
        val factory = ViewModelFactory(repository)
        
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        viewModel.registrationSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Perfil Guardado con Éxito", Toast.LENGTH_SHORT).show()
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
        binding.btnSave.setOnClickListener {
            val name = binding.tilName.editText?.text.toString()
            val email = binding.tilEmail.editText?.text.toString()
            val phone = binding.tilPhone.editText?.text.toString()
            val service = binding.tilService.editText?.text.toString()
            val exp = binding.tilExp.editText?.text.toString()

            viewModel.registerUser(name, email, phone, service, exp)
        }
    }
}