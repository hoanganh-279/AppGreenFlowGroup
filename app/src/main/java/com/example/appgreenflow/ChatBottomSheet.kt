package com.example.appgreenflow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ChatBottomSheet : BottomSheetDialogFragment() {
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnClose: ImageButton
    
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        rvMessages = view.findViewById(R.id.rvMessages)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)
        btnClose = view.findViewById(R.id.btnClose)
        
        setupRecyclerView()
        loadMessages()
        setupSendButton()
        
        btnClose.setOnClickListener { dismiss() }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages, userId)
        rvMessages.adapter = adapter
        rvMessages.layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }
    }

    private fun loadMessages() {
        db.collection("support_chats")
            .document(userId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Lỗi tải tin nhắn", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show()
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
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show()
                btnSend.isEnabled = true
            }
    }

    companion object {
        fun newInstance() = ChatBottomSheet()
    }
}
