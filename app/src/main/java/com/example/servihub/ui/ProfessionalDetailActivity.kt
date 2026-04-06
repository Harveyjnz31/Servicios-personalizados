package com.example.servihub.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityProfessionalDetailBinding
import com.example.servihub.model.Favorite
import com.example.servihub.model.Review
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfessionalDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfessionalDetailBinding
    private lateinit var viewModel: WelcomeViewModel
    private var isFavorite = false
    private var proId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfessionalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // System Bars Styling
        window.statusBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#F8FAFC")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao(), database.workRequestDao(), database.favoriteDao(), database.chatDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setupToolbar()
        loadProfessionalData()
        setupListeners()
        setupMockReviews()
        observeFavoriteStatus()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadProfessionalData() {
        val name = intent.getStringExtra("PRO_NAME") ?: "Profesional"
        val specialty = intent.getStringExtra("PRO_SPECIALTY") ?: "Especialista"
        val rating = intent.getStringExtra("PRO_RATING") ?: "4.9"
        val location = intent.getStringExtra("PRO_LOCATION") ?: "Centro"
        proId = name.hashCode()

        binding.tvDetailName.text = name
        binding.tvDetailSpecialty.text = specialty.uppercase()
        binding.tvDetailRating.text = rating
        binding.tvDetailLocation.text = location
        
        binding.tvJobsCount.text = "${(name.length * 7) + 12}+"
        binding.tvExpYears.text = "${(name.length % 8) + 2} años"
    }

    private fun observeFavoriteStatus() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                viewModel.isFavorite(it.id, proId).observe(this) { fav ->
                    isFavorite = fav
                    updateFavoriteIcon()
                }
            }
        }
    }

    private fun updateFavoriteIcon() {
        val icon = if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        binding.btnFavorite.setImageResource(icon)
        binding.btnFavorite.colorFilter = if (isFavorite) {
            android.graphics.PorterDuffColorFilter(Color.parseColor("#FF3B5C"), android.graphics.PorterDuff.Mode.SRC_IN)
        } else {
            android.graphics.PorterDuffColorFilter(Color.parseColor("#1E293B"), android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    private fun setupListeners() {
        binding.fabContact.setOnClickListener {
            val name = binding.tvDetailName.text.toString()
            val intent = Intent(this, ChatActivity::class.java).apply {
                putExtra("PRO_NAME", name)
                putExtra("PRO_ID", proId)
            }
            startActivity(intent)
        }

        binding.btnFavorite.setOnClickListener {
            val user = viewModel.userProfile.value
            if (user != null) {
                val favorite = Favorite(
                    clientId = user.id,
                    professionalId = proId,
                    professionalName = binding.tvDetailName.text.toString(),
                    professionalSpecialty = binding.tvDetailSpecialty.text.toString(),
                    professionalRating = binding.tvDetailRating.text.toString(),
                    professionalLocation = binding.tvDetailLocation.text.toString()
                )
                viewModel.toggleFavorite(favorite, isAdd = !isFavorite)
                val msg = if (!isFavorite) "Añadido a favoritos" else "Eliminado de favoritos"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRate.setOnClickListener {
            showRatingDialog()
        }
    }

    private fun showRatingDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val etComment = dialogView.findViewById<EditText>(R.id.etComment)

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Publicar") { _, _ ->
                val rating = ratingBar.rating
                val comment = etComment.text.toString()
                if (comment.isNotEmpty()) {
                    saveReview(rating, comment)
                } else {
                    Toast.makeText(this, "Por favor escribe un comentario", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun saveReview(rating: Float, comment: String) {
        val user = viewModel.userProfile.value
        if (user != null) {
            val review = Review(
                authorName = user.fullName,
                content = comment,
                rating = rating,
                timestamp = System.currentTimeMillis(),
                professionalId = proId
            )
            viewModel.insertReview(review)
            Toast.makeText(this@ProfessionalDetailActivity, "Reseña publicada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupMockReviews() {
        val reviews = listOf(
            Pair("Carlos Ruiz", "Excelente servicio, muy puntual y profesional."),
            Pair("Elena M.", "Hizo un trabajo impecable con la instalación eléctrica."),
            Pair("Roberto S.", "Muy recomendado, solucionó el problema rápidamente.")
        )

        binding.llDetailReviews.removeAllViews()
        val inflater = LayoutInflater.from(this)
        
        reviews.forEach { (author, comment) ->
            val itemView = inflater.inflate(R.layout.item_review_simple, binding.llDetailReviews, false)
            itemView.findViewById<TextView>(R.id.tvReviewAuthor).text = author
            itemView.findViewById<TextView>(R.id.tvReviewComment).text = comment
            binding.llDetailReviews.addView(itemView)
        }
    }
}
