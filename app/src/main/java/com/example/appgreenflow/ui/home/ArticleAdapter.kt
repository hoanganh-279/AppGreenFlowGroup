package com.example.appgreenflow.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgreenflow.R

class ArticleAdapter(
    private val articles: MutableList<Article>,  // Non-null list
    private val listener: OnArticleClickListener
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {  // Remove ? from ViewHolder

    interface OnArticleClickListener {
        fun onArticleClick(article: Article)  // Non-null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]  // Safe: Non-null list
        holder.bind(article)  // Use bind method for safety
    }

    override fun getItemCount(): Int = articles.size  // Fix: override fun, not val

    fun updateData(newArticles: List<Article>) {  // Non-null input
        articles.clear()
        articles.addAll(newArticles)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {  // Public inner
        val ivImage: ImageView = itemView.findViewById(R.id.ivArticleImage)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)

        fun bind(article: Article) {
            tvTitle.text = article.title ?: "No Title"
            tvDesc.text = article.desc ?: "No Description"
            Glide.with(itemView.context)
                .load(article.imageUrl)
                .placeholder(R.drawable.ic_image)  // Assume placeholder exists
                .into(ivImage)
            itemView.setOnClickListener { listener.onArticleClick(article) }
        }
    }
}