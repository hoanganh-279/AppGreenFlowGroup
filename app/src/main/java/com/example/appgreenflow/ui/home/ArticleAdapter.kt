package com.example.appgreenflow.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R

class ArticleAdapter(
    private var articles: MutableList<Article>,
    private val listener: OnArticleClickListener
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder?>() {
    interface OnArticleClickListener {
        fun onArticleClick(article: Article?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article: Article = articles.get(position)
        holder.tvTitle.setText(article.title)
        holder.tvDesc.setText(article.desc)
        holder.itemView.setOnClickListener(View.OnClickListener { v: View? ->
            listener.onArticleClick(
                article
            )
        })
    }

    val itemCount: Int
        get() = articles.size

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var ivImage: ImageView?
        var tvTitle: TextView
        var tvDesc: TextView

        init {
            ivImage = view.findViewById<ImageView?>(R.id.ivArticleImage)
            tvTitle = view.findViewById<TextView>(R.id.tvTitle)
            tvDesc = view.findViewById<TextView>(R.id.tvDesc)
        }
    }

    fun updateData(newArticles: MutableList<Article>) {
        this.articles = newArticles
        notifyDataSetChanged()
    }
}