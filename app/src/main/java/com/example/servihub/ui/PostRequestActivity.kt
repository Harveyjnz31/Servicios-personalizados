package com.example.servihub.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityPostRequestBinding
import com.example.servihub.model.WorkRequest
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class PostRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPostRequestBinding
    private lateinit var viewModel: WelcomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // System Bars Styling
        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#F8FAFC")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Initialize ViewModel
        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao(), database.workRequestDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setupToolbar()
        setupDropdown()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupDropdown() {
        val categories = arrayOf(
            "ELECTRICIDAD", "PLOMERÍA", "LIMPIEZA", "OBRA CIVIL",
            "SOLDADURA", "PINTURA", "MECÁNICA", "CARPINTERÍA", "TÉCNICO"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.autoCompleteCategory.setAdapter(adapter)
    }

    private fun setupListeners() {
        binding.btnPublish.setOnClickListener {
            publishRequest()
        }
    }

    private fun publishRequest() {
        val title = binding.etTitle.text.toString().trim()
        val specialty = binding.autoCompleteCategory.text.toString()
        val description = binding.etDescription.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val budgetStr = binding.etBudget.text.toString().trim()

        if (title.isEmpty() || specialty.isEmpty() || description.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val budget = budgetStr.toDoubleOrNull()
        val user = viewModel.userProfile.value

        if (user != null) {
            val request = WorkRequest(
                title = title,
                specialty = specialty,
                description = description,
                location = location,
                clientId = user.id,
                clientName = user.fullName,
                budget = budget
            )

            viewModel.insertWorkRequest(request)
            Toast.makeText(this, "Solicitud publicada exitosamente", Toast.LENGTH_LONG).show()
            finish()
        } else {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
        }
    }
}
