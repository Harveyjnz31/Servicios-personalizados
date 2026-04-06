package com.example.servihub.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.servihub.R
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityRequestsListBinding
import com.example.servihub.model.WorkRequest
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class RequestsListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRequestsListBinding
    private lateinit var viewModel: WelcomeViewModel
    private lateinit var adapter: RequestsAdapter
    private var currentCategory = "Todas"
    private var currentLocation = "Cualquier Lugar"
    private var allRequests: List<WorkRequest> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRequestsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = Color.WHITE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = Color.parseColor("#F1F5F9")
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao(), database.workRequestDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        adapter = RequestsAdapter(emptyList())
        binding.rvRequests.layoutManager = LinearLayoutManager(this)
        binding.rvRequests.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.allWorkRequests.observe(this) { requests ->
            allRequests = requests
            applyFilters()
        }
    }

    private fun setupListeners() {
        binding.chipGroup.setOnCheckedChangeListener { _, checkedId ->
            currentCategory = when (checkedId) {
                R.id.chipElectric -> "ELECTRICIDAD"
                R.id.chipPlumbing -> "PLOMERÍA"
                R.id.chipCleaning -> "LIMPIEZA"
                R.id.chipCivil -> "OBRA CIVIL"
                R.id.chipWelding -> "SOLDADURA"
                R.id.chipPainting -> "PINTURA"
                R.id.chipMechanic -> "MECÁNICA"
                R.id.chipCarpentry -> "CARPINTERÍA"
                else -> "Todas"
            }
            applyFilters()
        }

        binding.chipGroupFilters.setOnCheckedChangeListener { _, checkedId ->
            currentLocation = when (checkedId) {
                R.id.chipFilterNorth -> "Norte"
                R.id.chipFilterCenter -> "Centro"
                R.id.chipFilterSouth -> "Sur"
                R.id.chipFilterIndustrial -> "Industrial"
                else -> "Cualquier Lugar"
            }
            applyFilters()
        }
    }

    private fun applyFilters() {
        val filtered = allRequests.filter { request ->
            val categoryMatch = currentCategory == "Todas" || request.specialty == currentCategory
            val locationMatch = currentLocation == "Cualquier Lugar" || request.location.contains(currentLocation, ignoreCase = true)
            categoryMatch && locationMatch
        }

        adapter.updateList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvRequests.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    inner class RequestsAdapter(private var requests: List<WorkRequest>) :
        RecyclerView.Adapter<RequestsAdapter.ViewHolder>() {

        fun updateList(newList: List<WorkRequest>) {
            requests = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_request_example, parent, false)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val request = requests[position]
            holder.tvTitle.text = request.title
            holder.tvCategory.text = request.specialty
            holder.tvDescription.text = request.description
            holder.tvLocation.text = request.location
        }

        override fun getItemCount() = requests.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTitle: TextView = view.findViewById(R.id.tvRequestTitle)
            val tvCategory: TextView = view.findViewById(R.id.tvRequestCategory)
            val tvDescription: TextView = view.findViewById(R.id.tvRequestDescription)
            val tvLocation: TextView = view.findViewById(R.id.tvRequestLocation)
        }
    }
}
