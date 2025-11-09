package com.example.appgreenflow

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        
        supportActionBar?.title = "Chat Hỗ Trợ"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        initViews()
        setupRecyclerView()
        loadMessages()
        setupSendButton()
    }

    private fun initViews() {
        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages, userId)
        rvMessages.adapter = adapter
        rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
    }

    private fun loadMessages() {
        progressBar.visibility = View.VISIBLE
        
        db.collection("support_chats")
            .document(userId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(this, "Lỗi tải tin nhắn: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                
                messages.clear()
                snapshot?.documents?.forEach { doc ->
                    val message = doc.toObject(ChatMessage::class.java)
                    message?.let { messages.add(it) }
                }
                
                adapter.notifyDataSetChanged()
                if (messages.isNotEmpty()) {
                    rvMessages.scrollToPosition(messages.size - 1)
                }
            }
    }

    private fun setupSendButton() {
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            sendMessage(text)
        }
    }

    private fun sendMessage(text: String) {
        val message = ChatMessage(
            senderId = userId,
            senderName = auth.currentUser?.displayName ?: "User",
            message = text,
            timestamp = System.currentTimeMillis(),
            isFromUser = true
        )
        
        btnSend.isEnabled = false
        
        db.collection("support_chats")
            .document(userId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener {
                etMessage.text.clear()
                btnSend.isEnabled = true
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Lỗi gửi tin nhắn: ${e.message}", Toast.LENGTH_SHORT).show()
                btnSend.isEnabled = true
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

data class ChatMessage(
    var senderId: String = "",
    var senderName: String = "",
    var message: String = "",
    var timestamp: Long = 0,
    var isFromUser: Boolean = true
)
