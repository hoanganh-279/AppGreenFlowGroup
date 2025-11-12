package com.example.appgreenflow.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.appgreenflow.R

class ArticleAdapter(
    private val articles: MutableList<Article>,
    private val listener: OnArticleClickListener
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    interface OnArticleClickListener {
        fun onArticleClick(article: Article)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    // Tối ưu với DiffUtil
    fun updateData(newArticles: List<Article>) {
        val diffCallback = ArticleDiffCallback(articles, newArticles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        articles.clear()
        articles.addAll(newArticles)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivImage: ImageView = itemView.findViewById(R.id.ivArticleImage)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvDesc: TextView = itemView.findViewById(R.id.tvDesc)

        fun bind(article: Article) {
            tvTitle.text = article.title ?: "Không có tiêu đề"
            tvDesc.text = article.desc ?: "Không có mô tả"
            
            // Tối ưu Glide với caching
            Glide.with(itemView.context)
                .load(article.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_image_placeholder)
                .centerCrop()
                .into(ivImage)
            
            itemView.setOnClickListener { listener.onArticleClick(article) }
        }
    }
    
    // DiffUtil callback để tối ưu RecyclerView
    private class ArticleDiffCallback(
        private val oldList: List<Article>,
        private val newList: List<Article>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].title == newList[newItemPosition].title
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}