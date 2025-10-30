package com.example.appgreenflow.ui.notifications;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.appgreenflow.MainActivity;
import com.example.appgreenflow.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NotificationsFragment extends Fragment {
    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private String photoUrl = "";
    private ImageView ivPhoto;
    private ActivityResultLauncher<Intent> cameraLauncher;

    public static NotificationsFragment newInstance() {
        return new NotificationsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        setupCameraLauncher();
    }

    private void setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                            Bundle extras = result.getData().getExtras();
                            Bitmap photo = (Bitmap) extras.get("data");
                            if (photo != null && ivPhoto != null) {
                                ivPhoto.setImageBitmap(photo);
                                uploadPhotoToStorage(photo);  // Upload ngay sau khi chụp
                            }
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        rvNotifications = view.findViewById(R.id.rvNotifications);
        setupRecycler();
        loadNotifications();
        return view;
    }

    private void setupRecycler() {
        List<Notification> notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications, notification -> {
            String role = ((MainActivity) requireActivity()).getUserRole();
            if ("employee".equals(role)) {
                db.collection("notifications").document(notification.id).update("status", "collected");
                Toast.makeText(getContext(), "Đã xác nhận thu gom!", Toast.LENGTH_SHORT).show();
            } else {
                showReportDialog();
            }
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
                    List<Notification> notifications = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Notification notif = doc.toObject(Notification.class);
                            if (notif != null) {
                                notifications.add(notif);
                            }
                        }
                    }
                    if (adapter != null) {
                        adapter.updateData(notifications);
                    }
                });
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Báo cáo lỗi thùng rác");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        builder.setView(dialogView);

        Spinner spinnerIssue = dialogView.findViewById(R.id.spinnerReportIssue);
        TextInputEditText etDesc = dialogView.findViewById(R.id.etReportDesc);
        ivPhoto = dialogView.findViewById(R.id.ivReportPhoto);

        if (ivPhoto != null) {
            ivPhoto.setOnClickListener(v -> launchCamera());
        }

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String issue = spinnerIssue.getSelectedItem().toString();
            String desc = etDesc.getText().toString().trim();
            if (desc.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng mô tả lỗi!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> report = new HashMap<>();
            report.put("issue", issue);
            report.put("desc", desc);
            report.put("photoUrl", photoUrl);
            report.put("userId", FirebaseAuth.getInstance().getUid());
            report.put("timestamp", System.currentTimeMillis());
            report.put("role", ((MainActivity) requireActivity()).getUserRole());

            FirebaseFirestore.getInstance().collection("reports").add(report)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Báo cáo đã gửi thành công!" + (photoUrl.isEmpty() ? "" : " với ảnh!"), Toast.LENGTH_SHORT).show();
                        sendFCMNotification("new_report");
                        dialog.dismiss();
                        photoUrl = "";  // Reset
                        if (ivPhoto != null) ivPhoto.setImageBitmap(null);
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi gửi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {
            dialog.dismiss();
            photoUrl = "";  // Reset nếu hủy
            if (ivPhoto != null) ivPhoto.setImageBitmap(null);
        });
        builder.show();
    }

    private void launchCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            cameraLauncher.launch(cameraIntent);
        } else {
            Toast.makeText(getContext(), "Không hỗ trợ camera!", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotoToStorage(Bitmap photo) {
        if (photo == null) return;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        String fileName = "report_" + UUID.randomUUID().toString() + ".jpg";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("reports/" + fileName);

        storageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        photoUrl = uri.toString();
                        Toast.makeText(getContext(), "Ảnh đã upload!", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Lỗi lấy URL ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        photoUrl = "";
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Lỗi upload ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    photoUrl = "";
                });
    }

    private void sendFCMNotification(String type) {
        Map<String, String> data = new HashMap<>();
        data.put("type", type);
        data.put("message", "Có báo cáo mới từ khách hàng!");

        RemoteMessage remoteMessage = new RemoteMessage.Builder("reports-topic")
                .setData(data)
                .build();

        FirebaseMessaging.getInstance().send(remoteMessage)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Toast.makeText(getContext(), "Lỗi gửi thông báo FCM!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}