package com.example.servihub.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityProfileDetailsBinding
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel
import java.net.URLEncoder

class ProfileDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileDetailsBinding
    private val viewModel: WelcomeViewModel by viewModels {
        ViewModelFactory(UserRepository(AppDatabase.getDatabase(this).userDao()))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Estilo formal para las barras del sistema
        window.statusBarColor = Color.parseColor("#FFFFFF")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#F1F5F9")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or 
                                                 View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

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
                
                val isProfessional = it.userRole == "PROFESSIONAL"
                
                // Assign role-based avatar in details as well
                if (isProfessional) {
                    binding.ivAvatar.setImageResource(android.R.drawable.ic_menu_manage)
                } else {
                    binding.ivAvatar.setImageResource(android.R.drawable.ic_menu_myplaces)
                }
                
                binding.tvRole.text = if (isProfessional) getString(R.string.role_professional) else getString(R.string.role_client)
                
                if (isProfessional) {
                    binding.llProfessionalDetails.visibility = View.VISIBLE
                    binding.tvExperience.text = getString(R.string.years_suffix, it.experienceYears.toString())
                    binding.tvSpecialty.text = it.serviceType
                    
                    // Show contact button for professionals
                    binding.btnContact.visibility = View.VISIBLE
                    binding.btnContact.setOnClickListener { _ ->
                        contactProfessional(it.phone, it.fullName)
                    }
                } else {
                    binding.llProfessionalDetails.visibility = View.GONE
                    binding.btnContact.visibility = View.GONE
                }
            }
        }
    }

    private fun contactProfessional(phone: String, name: String) {
        val message = "Hola $name, vi tu perfil en ServiHub y me gustaría contactarte."
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            val url = "https://api.whatsapp.com/send?phone=$phone&text=" + URLEncoder.encode(message, "UTF-8")
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback to direct call if WhatsApp fails
            val callIntent = Intent(Intent.ACTION_DIAL)
            callIntent.data = Uri.parse("tel:$phone")
            startActivity(callIntent)
            Toast.makeText(this, "Redirigiendo a llamada directa", Toast.LENGTH_SHORT).show()
        }
    }
}
