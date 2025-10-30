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
        Glide.with(holder.itemView.context).load(R.drawable.ic_trash_full).into(holder.ivIcon)
        val context = holder.itemView.context
        if (context is MainActivity) {
            role = context.userRole.orEmpty()  // Assuming MainActivity has 'userRole: String' property; adjust if method
        }
        holder.btnConfirm.visibility = if (role == "employee") View.VISIBLE else View.GONE

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
        val btnConfirm: Button = view.findViewById(R.id.btnConfirm)
    }

    fun updateData(newNotifications: List<Notification>) {
        notifications.clear()
        notifications.addAll(newNotifications)
        notifyDataSetChanged()  // Consider DiffUtil for efficiency in production
    }
}