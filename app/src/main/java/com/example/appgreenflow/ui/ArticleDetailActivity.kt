package com.example.appgreenflow.ui

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.appgreenflow.ChatHelper
import com.example.appgreenflow.R

class ArticleDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_detail)

        val tvTitle = findViewById<TextView>(R.id.tvDetailTitle)
        val tvContent = findViewById<TextView>(R.id.tvDetailContent)

        val extras = getIntent().getExtras()
        if (extras != null) {
            tvTitle.setText(extras.getString("title", "Không có tiêu đề"))
            tvContent.setText(extras.getString("content", "Không có nội dung"))
        } else {
            Toast.makeText(this, "Lỗi load bài báo: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT)
                .show()
            finish()
        }
        
        // Thêm chat button
        ChatHelper.addChatButton(this)
    }
}