package com.example.appgreenflow.ui.notifications

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R
import com.example.appgreenflow.ui.notifications.Notification  // Adjust to correct package
import com.example.appgreenflow.ui.notifications.NotificationAdapter
import com.example.appgreenflow.ui.notifications.NotificationAdapter.OnNotificationClickListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.android.material.textfield.TextInputEditText

class NotificationsFragment : Fragment() {
    private var rvNotifications: RecyclerView? = null
    private var adapter: NotificationAdapter? = null
    private var db: FirebaseFirestore? = null
    private var photoUrl: String? = null
    private var ivPhoto: ImageView? = null
    private var cameraLauncher: ActivityResultLauncher<Intent>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        setupCameraLauncher()
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                @Suppress("DEPRECATION")  // For compatibility; consider CameraX for modern apps
                val photo = result.data?.extras?.getParcelable<Bitmap>("data")
                if (photo != null && ivPhoto != null) {
                    ivPhoto?.setImageBitmap(photo)
                    uploadPhotoToStorage(photo)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        rvNotifications = view.findViewById(R.id.rvNotifications)
        setupRecycler()
        loadNotifications()
        return view
    }

    private fun setupRecycler() {
        val recyclerView = rvNotifications ?: return
        adapter = NotificationAdapter(
            mutableListOf(),
            object : OnNotificationClickListener {  // Explicit object to resolve SAM/constructor issue
                override fun onNotificationClick(notification: Notification) {
                    val role = (requireActivity() as MainActivity).userRole.orEmpty()
                    if (role == "employee") {
                        db?.collection("notifications")?.document(notification.id.orEmpty())
                            ?.update("status", "collected")
                        context?.let { ctx ->
                            Toast.makeText(ctx, "Đã xác nhận thu gom!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        showReportDialog()
                    }
                    (requireActivity() as MainActivity).loadRouteFragment(
                        notification.location.orEmpty(),
                        notification.lat,
                        notification.lng,
                        notification.percent
                    )
                }
            }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context, 2)
    }

    private fun loadNotifications() {
        db?.collection("notifications")
            ?.whereGreaterThan("percent", 70)
            ?.orderBy("timestamp", Query.Direction.DESCENDING)
            ?.addSnapshotListener(EventListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    context?.let { ctx ->
                        Toast.makeText(
                            ctx,
                            "Lỗi load thông báo: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@EventListener
                }
                val notifications = mutableListOf<Notification>()
                snapshot?.documents?.forEach { doc ->
                    val notif = doc.toObject(Notification::class.java)
                    if (notif != null) {
                        notifications.add(notif)
                    }
                }
                adapter?.updateData(notifications)
            })
    }

    private fun showReportDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Báo cáo lỗi thùng rác")

        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report, null)
        builder.setView(dialogView)

        val spinnerIssue: Spinner = dialogView.findViewById(R.id.spinnerReportIssue)
        val etDesc: TextInputEditText = dialogView.findViewById(R.id.etReportDesc)
        ivPhoto = dialogView.findViewById(R.id.ivReportPhoto)

        ivPhoto?.setOnClickListener { launchCamera() }

        builder.setPositiveButton(
            "Gửi"
        ) { dialog: DialogInterface?, which: Int ->
            val issue = spinnerIssue.selectedItem.toString()
            val desc = etDesc.text.toString().trim()
            if (desc.isEmpty()) {
                context?.let { ctx ->
                    Toast.makeText(ctx, "Vui lòng mô tả lỗi!", Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val report: MutableMap<String, Any> = mutableMapOf()
            report["issue"] = issue
            report["desc"] = desc
            report["photoUrl"] = photoUrl.orEmpty()
            report["userId"] = FirebaseAuth.getInstance().uid.orEmpty()
            report["timestamp"] = System.currentTimeMillis()
            report["role"] = (requireActivity() as MainActivity).userRole.orEmpty()
            FirebaseFirestore.getInstance().collection("reports").add(report)
                .addOnSuccessListener { documentReference: DocumentReference ->
                    context?.let { ctx ->
                        Toast.makeText(
                            ctx,
                            "Báo cáo đã gửi thành công!" + if (photoUrl.isNullOrEmpty()) "" else " với ảnh!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // FCM sending decommissioned; use Cloud Function trigger on report creation for notifications
                    // sendFCMNotification("new_report")
                    dialog?.dismiss()
                    photoUrl = null
                    ivPhoto?.setImageBitmap(null)
                }
                .addOnFailureListener { e: Exception ->
                    context?.let { ctx ->
                        Toast.makeText(
                            ctx,
                            "Lỗi gửi: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        builder.setNegativeButton(
            "Hủy"
        ) { dialog: DialogInterface?, which: Int ->
            dialog?.dismiss()
            photoUrl = null
            ivPhoto?.setImageBitmap(null)
        }
        builder.show()
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            cameraLauncher?.launch(cameraIntent)
        } else {
            context?.let { ctx ->
                Toast.makeText(ctx, "Không hỗ trợ camera!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPhotoToStorage(photo: Bitmap) {
        val baos = java.io.ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val fileName = "report_${java.util.UUID.randomUUID()}.jpg"
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child("reports/$fileName")

        storageRef.putBytes(data)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri: Uri ->
                    photoUrl = uri.toString()
                    context?.let { ctx ->
                        Toast.makeText(ctx, "Ảnh đã upload!", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { e: Exception ->
                    context?.let { ctx ->
                        Toast.makeText(
                            ctx,
                            "Lỗi lấy URL ảnh: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    photoUrl = null
                }
            }
            .addOnFailureListener { e: Exception ->
                context?.let { ctx ->
                    Toast.makeText(ctx, "Lỗi upload ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                photoUrl = null
            }
    }

    // Deprecated and decommissioned; remove or migrate to server-side Cloud Function
    @Suppress("DEPRECATION")
    private fun sendFCMNotification(type: String) {
        val data: MutableMap<String, String> = mutableMapOf()
        data["type"] = type
        data["message"] = "Có báo cáo mới từ khách hàng!"

        val remoteMessage = RemoteMessage.Builder("reports-topic")
            .setData(data)
            .build()

        FirebaseMessaging.getInstance().send(remoteMessage)
        // Note: send() returns void (fire-and-forget); no async callback available.
        // For error handling, consider logging or server-side alternatives.
        Log.d("FCM", "Message sent to topic: reports-topic")
    }

    companion object {
        @JvmStatic
        @Suppress("UNUSED")  // Suppress if not used elsewhere
        fun newInstance(): NotificationsFragment = NotificationsFragment()
    }
}