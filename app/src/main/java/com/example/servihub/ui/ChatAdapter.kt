package com.example.servihub.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.servihub.R
import com.example.servihub.model.ChatMessage
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(private val currentUserId: Int) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var messages: List<ChatMessage> = emptyList()

    fun setMessages(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) VIEW_TYPE_SENT else VIEW_TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val layout = if (viewType == VIEW_TYPE_SENT) R.layout.item_chat_sent else R.layout.item_chat_received
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageText: TextView = view.findViewById(R.id.tvMessage)
        private val timeText: TextView = view.findViewById(R.id.tvTime)

        fun bind(message: ChatMessage) {
            messageText.text = message.message
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            timeText.text = sdf.format(Date(message.timestamp))
        }
    }

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }
}
