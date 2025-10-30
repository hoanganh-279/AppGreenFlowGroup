package com.example.appgreenflow.ui.notifications

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.MainActivity
import com.example.appgreenflow.R
import com.example.appgreenflow.ui.notifications.NotificationAdapter.OnNotificationClickListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
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
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.util.UUID

class NotificationsFragment : Fragment() {
    private var rvNotifications: RecyclerView? = null
    private var adapter: NotificationAdapter? = null
    private var db: FirebaseFirestore? = null
    private var photoUrl = ""
    private var ivPhoto: ImageView? = null
    private var cameraLauncher: ActivityResultLauncher<Intent?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        setupCameraLauncher()
    }

    private fun setupCameraLauncher() {
        cameraLauncher = registerForActivityResult<Intent?, ActivityResult?>(
            StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult) {
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        val extras = result.data!!.getExtras()
                        val photo = extras!!.get("data") as Bitmap?
                        if (photo != null && ivPhoto != null) {
                            ivPhoto!!.setImageBitmap(photo)
                            uploadPhotoToStorage(photo) // Upload ngay sau khi chụp
                        }
                    }
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_notification, container, false)
        rvNotifications = view.findViewById<RecyclerView>(R.id.rvNotifications)
        setupRecycler()
        loadNotifications()
        return view
    }

    private fun setupRecycler() {
        val notifications: MutableList<Notification?> = ArrayList<Notification?>()
        adapter = NotificationAdapter(
            notifications,
            OnNotificationClickListener { notification: Notification? ->
                val role = (requireActivity() as MainActivity).getUserRole()
                if ("employee" == role) {
                    db!!.collection("notifications").document(notification!!.id!!)
                        .update("status", "collected")
                    Toast.makeText(getContext(), "Đã xác nhận thu gom!", Toast.LENGTH_SHORT).show()
                } else {
                    showReportDialog()
                }
                (requireActivity() as MainActivity).loadRouteFragment(
                    notification!!.location,
                    notification.lat,
                    notification.lng,
                    notification.percent
                )
            })
        rvNotifications!!.setAdapter(adapter)
        rvNotifications!!.setLayoutManager(GridLayoutManager(getContext(), 2))
    }

    private fun loadNotifications() {
        db!!.collection("notifications")
            .whereGreaterThan("percent", 70)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener(EventListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Toast.makeText(
                        getContext(),
                        "Lỗi load thông báo: " + error.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addSnapshotListener
                }
                val notifications: MutableList<Notification?> = ArrayList<Notification?>()
                if (snapshot != null) {
                    for (doc in snapshot.getDocuments()) {
                        val notif = doc.toObject<Notification?>(Notification::class.java)
                        if (notif != null) {
                            notifications.add(notif)
                        }
                    }
                }
                if (adapter != null) {
                    adapter!!.updateData(notifications)
                }
            })
    }

    private fun showReportDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Báo cáo lỗi thùng rác")

        val dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null)
        builder.setView(dialogView)

        val spinnerIssue = dialogView.findViewById<Spinner>(R.id.spinnerReportIssue)
        val etDesc = dialogView.findViewById<TextInputEditText>(R.id.etReportDesc)
        ivPhoto = dialogView.findViewById<ImageView?>(R.id.ivReportPhoto)

        if (ivPhoto != null) {
            ivPhoto!!.setOnClickListener(View.OnClickListener { v: View? -> launchCamera() })
        }

        builder.setPositiveButton(
            "Gửi",
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                val issue = spinnerIssue.getSelectedItem().toString()
                val desc = etDesc.getText().toString().trim { it <= ' ' }
                if (desc.isEmpty()) {
                    Toast.makeText(getContext(), "Vui lòng mô tả lỗi!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val report: MutableMap<String?, Any?> = HashMap<String?, Any?>()
                report.put("issue", issue)
                report.put("desc", desc)
                report.put("photoUrl", photoUrl)
                report.put("userId", FirebaseAuth.getInstance().getUid())
                report.put("timestamp", System.currentTimeMillis())
                report.put("role", (requireActivity() as MainActivity).getUserRole())
                FirebaseFirestore.getInstance().collection("reports").add(report)
                    .addOnSuccessListener(OnSuccessListener { aVoid: DocumentReference? ->
                        Toast.makeText(
                            getContext(),
                            "Báo cáo đã gửi thành công!" + (if (photoUrl.isEmpty()) "" else " với ảnh!"),
                            Toast.LENGTH_SHORT
                        ).show()
                        sendFCMNotification("new_report")
                        dialog!!.dismiss()
                        photoUrl = "" // Reset
                        if (ivPhoto != null) ivPhoto!!.setImageBitmap(null)
                    })
                    .addOnFailureListener(OnFailureListener { e: Exception? ->
                        Toast.makeText(
                            getContext(),
                            "Lỗi gửi: " + e!!.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    })
            })

        builder.setNegativeButton(
            "Hủy",
            DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                dialog!!.dismiss()
                photoUrl = "" // Reset nếu hủy
                if (ivPhoto != null) ivPhoto!!.setImageBitmap(null)
            })
        builder.show()
    }

    private fun launchCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            cameraLauncher!!.launch(cameraIntent)
        } else {
            Toast.makeText(getContext(), "Không hỗ trợ camera!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadPhotoToStorage(photo: Bitmap?) {
        if (photo == null) return

        val baos = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val fileName = "report_" + UUID.randomUUID().toString() + ".jpg"
        val storageRef = FirebaseStorage.getInstance().getReference().child("reports/" + fileName)

        storageRef.putBytes(data)
            .addOnSuccessListener(OnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                storageRef.getDownloadUrl().addOnSuccessListener(OnSuccessListener { uri: Uri? ->
                    photoUrl = uri.toString()
                    Toast.makeText(getContext(), "Ảnh đã upload!", Toast.LENGTH_SHORT).show()
                }).addOnFailureListener(OnFailureListener { e: Exception? ->
                    Toast.makeText(
                        getContext(),
                        "Lỗi lấy URL ảnh: " + e!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    photoUrl = ""
                })
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                Toast.makeText(getContext(), "Lỗi upload ảnh: " + e!!.message, Toast.LENGTH_SHORT)
                    .show()
                photoUrl = ""
            })
    }

    private fun sendFCMNotification(type: String?) {
        val data: MutableMap<String?, String?> = HashMap<String?, String?>()
        data.put("type", type)
        data.put("message", "Có báo cáo mới từ khách hàng!")

        val remoteMessage = RemoteMessage.Builder("reports-topic")
            .setData(data)
            .build()

        FirebaseMessaging.getInstance().send(remoteMessage)
            .addOnCompleteListener({ task ->
                if (!task.isSuccessful()) {
                    Toast.makeText(getContext(), "Lỗi gửi thông báo FCM!", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    companion object {
        fun newInstance(): NotificationsFragment {
            return NotificationsFragment()
        }
    }
}