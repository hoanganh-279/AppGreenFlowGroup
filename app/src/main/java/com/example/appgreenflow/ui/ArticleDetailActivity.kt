package com.example.appgreenflow.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgreenflow.R;

public class ArticleDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvContent = findViewById(R.id.tvDetailContent);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvTitle.setText(extras.getString("title", "Không có tiêu đề"));
            tvContent.setText(extras.getString("content", "Không có nội dung"));
        } else {
            Toast.makeText(this, "Lỗi load bài báo: Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}