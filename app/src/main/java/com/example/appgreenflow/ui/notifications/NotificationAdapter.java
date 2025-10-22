package com.example.appgreenflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<Notification> notifications;
    private OnNotificationClickListener listener;
    private String role;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notif = notifications.get(position);
        holder.tvLocation.setText(notif.location);
        holder.tvPercent.setText(notif.percent + "%");
        Glide.with(holder.itemView.getContext()).load(R.drawable.ic_trash_full).into(holder.ivIcon);
        role = ((AppCompatActivity) holder.itemView.getContext()).getUserRole();  // Lấy role từ Main
        holder.btnConfirm.setVisibility("employee".equals(role) ? View.VISIBLE : View.GONE);
        holder.cardView.setOnClickListener(v -> listener.onNotificationClick(notif));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivIcon;
        TextView tvLocation, tvPercent;
        Button btnConfirm;

        ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardNotification);
            ivIcon = view.findViewById(R.id.ivIcon);
            tvLocation = view.findViewById(R.id.tvLocation);
            tvPercent = view.findViewById(R.id.tvPercent);
            btnConfirm = view.findViewById(R.id.btnConfirm);
        }
    }
}