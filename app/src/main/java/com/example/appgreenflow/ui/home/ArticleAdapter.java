package com.example.appgreenflow.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;  // Nếu dùng Glide, thêm dependency: implementation 'com.github.bumptech.glide:glide:4.16.0'
import com.example.appgreenflow.Article;
import com.example.appgreenflow.R;

import java.util.List;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private List<Article> articles;
    private OnArticleClickListener listener;

    public interface OnArticleClickListener {
        void onArticleClick(Article article);
    }

    public ArticleAdapter(List<Article> articles, OnArticleClickListener listener) {
        this.articles = articles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.tvTitle.setText(article.title);
        holder.tvDesc.setText(article.desc);
        // Load image nếu có (thêm Glide nếu dùng)
        // Glide.with(holder.itemView.getContext()).load(article.imageUrl).into(holder.ivImage);
        holder.itemView.setOnClickListener(v -> listener.onArticleClick(article));
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvDesc;

        ViewHolder(View view) {
            super(view);
            ivImage = view.findViewById(R.id.ivArticleImage);
            tvTitle = view.findViewById(R.id.tvTitle);
            tvDesc = view.findViewById(R.id.tvDesc);
        }
    }
}