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

class NotificationAdapter(
    private var notifications: MutableList<Notification>?,
    private val listener: OnNotificationClickListener
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder?>() {
    private var role: String? = "customer"

    interface OnNotificationClickListener {
        fun onNotificationClick(notification: Notification?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notif = notifications!!.get(position)
        holder.tvLocation.setText(notif.location)
        holder.tvPercent.setText(notif.percent.toString() + "%")
        Glide.with(holder.itemView.getContext()).load(R.drawable.ic_trash_full).into(holder.ivIcon)
        if (holder.itemView.getContext() is MainActivity) {
            role = (holder.itemView.getContext() as MainActivity).getUserRole()
        }
        holder.btnConfirm.setVisibility(if ("employee" == role) View.VISIBLE else View.GONE)

        holder.cardView.setOnClickListener(View.OnClickListener { v: View? ->
            listener.onNotificationClick(
                notif
            )
        })
    }

    override fun getItemCount(): Int {
        return if (notifications != null) notifications!!.size else 0
    }

    internal class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var cardView: CardView
        var ivIcon: ImageView
        var tvLocation: TextView
        var tvPercent: TextView
        var btnConfirm: Button

        init {
            cardView = view.findViewById<CardView>(R.id.cardNotification)
            ivIcon = view.findViewById<ImageView>(R.id.ivIcon)
            tvLocation = view.findViewById<TextView>(R.id.tvLocation)
            tvPercent = view.findViewById<TextView>(R.id.tvPercent)
            btnConfirm = view.findViewById<Button>(R.id.btnConfirm)
        }
    }

    fun updateData(newNotifications: MutableList<Notification>?) {
        this.notifications = newNotifications
        notifyDataSetChanged()
    }
}