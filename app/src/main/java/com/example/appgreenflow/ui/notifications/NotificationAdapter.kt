package com.example.appgreenflow.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R
import com.example.appgreenflow.ui.notifications.Notification  // Adjust package if Notification is elsewhere

class NotificationAdapter(
    private val notifications: MutableList<Notification> = mutableListOf(),
    private val listener: OnNotificationClickListener
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    private var role: String = "customer"

    interface OnNotificationClickListener {
        fun onNotificationClick(notification: Notification)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notif = notifications[position]
        holder.tvLocation.text = notif.location
        holder.tvPercent.text = "${notif.percent}%"
        
        // Chọn icon và màu dựa trên mức độ đầy
        val iconRes = when {
            notif.percent >= 90 -> R.drawable.ic_trash_full
            notif.percent >= 70 -> R.drawable.ic_trash_half
            else -> R.drawable.ic_trash_half
        }
        
        val colorRes = when {
            notif.percent >= 90 -> android.R.color.holo_red_dark
            notif.percent >= 70 -> android.R.color.holo_orange_dark
            else -> android.R.color.darker_gray
        }
        
        Glide.with(holder.itemView.context).load(iconRes).into(holder.ivIcon)
        holder.tvPercent.setTextColor(holder.itemView.context.getColor(colorRes))
        
        // Hiển thị trạng thái
        val statusText = when (notif.status) {
            "collected" -> "Đã thu gom"
            "pending" -> "Chưa thu gom"
            else -> "Chưa xử lý"
        }
        holder.tvStatus.text = statusText
        holder.tvStatus.setTextColor(
            holder.itemView.context.getColor(
                if (notif.status == "collected") android.R.color.holo_green_dark 
                else android.R.color.holo_orange_dark
            )
        )
        
        val context = holder.itemView.context
        if (context is MainActivity) {
            role = context.userRole.orEmpty()
        }
        
        // Chỉ hiển thị nút xác nhận cho employee và khi chưa thu gom
        holder.btnConfirm.visibility = if (role == "employee" && notif.status != "collected") 
            View.VISIBLE else View.GONE
        
        holder.btnConfirm.setOnClickListener {
            listener.onNotificationClick(notif)
        }

        holder.cardView.setOnClickListener {
            listener.onNotificationClick(notif)
        }
    }

    override fun getItemCount(): Int = notifications.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardView: CardView = view.findViewById(R.id.cardNotification)
        val ivIcon: ImageView = view.findViewById(R.id.ivIcon)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvPercent: TextView = view.findViewById(R.id.tvPercent)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirm)
    }

    fun updateData(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()  // Consider DiffUtil for efficiency in production
    }
}