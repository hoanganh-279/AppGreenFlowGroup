package com.example.appgreenflow.ui.report

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgreenflow.R

class ImageAdapter(
    private val images: List<Uri>,
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.ivImage)
        val btnRemove: ImageButton = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_image, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = images[position]
        
        Glide.with(holder.itemView.context)
            .load(uri)
            .centerCrop()
            .into(holder.imageView)
        
        holder.btnRemove.setOnClickListener {
            onRemove(position)
        }
    }

    override fun getItemCount() = images.size
}
