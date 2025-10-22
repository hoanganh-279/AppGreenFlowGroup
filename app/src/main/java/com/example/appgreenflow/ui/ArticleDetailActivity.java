package com.example.appgreenflow.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appgreenflow.R;

public class ArticleDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);  // Tạo layout đơn giản với TextView

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvContent = findViewById(R.id.tvDetailContent);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            tvTitle.setText(extras.getString("title"));
            tvContent.setText(extras.getString("content"));
        } else {
            Toast.makeText(this, "Không có dữ liệu bài báo!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}