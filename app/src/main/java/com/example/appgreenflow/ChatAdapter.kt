package com.example.appgreenflow

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.bind(message, message.senderId == currentUserId)
    }

    override fun getItemCount() = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.messageContainer)

        fun bind(message: ChatMessage, isCurrentUser: Boolean) {
            tvMessage.text = message.message
            tvTime.text = formatTime(message.timestamp)
            
            // Căn lề tin nhắn
            val params = messageContainer.layoutParams as LinearLayout.LayoutParams
            if (isCurrentUser) {
                params.gravity = Gravity.END
                messageContainer.setBackgroundResource(R.drawable.bg_message_sent)
            } else {
                params.gravity = Gravity.START
                messageContainer.setBackgroundResource(R.drawable.bg_message_received)
            }
            messageContainer.layoutParams = params
        }

        private fun formatTime(timestamp: Long): String {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }
    }
}
