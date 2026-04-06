package com.example.servihub.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.servihub.data.AppDatabase
import com.example.servihub.data.UserRepository
import com.example.servihub.databinding.ActivityChatBinding
import com.example.servihub.model.ChatMessage
import com.example.servihub.viewmodel.ViewModelFactory
import com.example.servihub.viewmodel.WelcomeViewModel

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var viewModel: WelcomeViewModel
    private lateinit var adapter: ChatAdapter
    private var currentUserId: Int = -1
    private var otherUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val database = AppDatabase.getDatabase(this)
        val repository = UserRepository(database.userDao(), database.reviewDao(), database.workRequestDao(), database.favoriteDao(), database.chatDao())
        val factory = ViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[WelcomeViewModel::class.java]

        val proName = intent.getStringExtra("PRO_NAME") ?: "Profesional"
        otherUserId = intent.getIntExtra("PRO_ID", -1)
        
        binding.toolbar.title = proName
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        viewModel.userProfile.observe(this) { profile ->
            if (profile != null) {
                currentUserId = profile.id
                setupRecyclerView()
                observeMessages()
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty() && currentUserId != -1 && otherUserId != -1) {
                val message = ChatMessage(
                    senderId = currentUserId,
                    receiverId = otherUserId,
                    message = text
                )
                viewModel.sendMessage(message)
                binding.etMessage.setText("")
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(currentUserId)
        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter
    }

    private fun observeMessages() {
        viewModel.getMessages(currentUserId, otherUserId).observe(this) { messages ->
            adapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                binding.rvMessages.smoothScrollToPosition(messages.size - 1)
            }
        }
    }
}
