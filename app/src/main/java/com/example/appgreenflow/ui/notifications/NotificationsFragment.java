package com.example.appgreenflow.ui.notifications;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.NotificationAdapter;  // Adapter mới
import com.example.appgreenflow.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {
    private NotificationsViewModel mViewModel;
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private List<Notification> notifications = new ArrayList<>();  // Data class Notification
    private FirebaseFirestore db;

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        setupRecycler();
        loadNotifications();
        return view;
    }

    private void setupRecycler() {
        adapter = new NotificationAdapter(notifications, notification -> {
            String role = ((MainActivity) requireActivity()).getUserRole();
            if ("employee".equals(role)) {
                // Xác nhận thu gom
                db.collection("notifications").document(notification.id).update("status", "collected");
                Toast.makeText(getContext(), "Đã xác nhận thu gom!", Toast.LENGTH_SHORT).show();
            } else {
                // Customer báo lỗi
                showReportDialog();
            }
            // Navigate to route
            ((MainActivity) requireActivity()).loadRouteFragment(notification.location, notification.lat, notification.lng, notification.percent);
        });
        rvNotifications.setAdapter(adapter);
        rvNotifications.setLayoutManager(new GridLayoutManager(getContext(), 2));
    }

    private void loadNotifications() {
        db.collection("notifications")
                .whereGreaterThan("percent", 70)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Lỗi load thông báo: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    notifications.clear();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Notification notif = doc.toObject(Notification.class);
                            if (notif != null) notifications.add(notif);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Báo cáo lỗi thùng rác");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);  // Tạo layout đơn giản với Spinner + EditText
        builder.setView(dialogView);
        builder.setPositiveButton("Gửi", (dialog, which) -> {
            // Lấy data từ dialogView, add to Firestore "reports"
            Toast.makeText(getContext(), "Báo cáo đã gửi!", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }
}