package com.example.appgreenflow.ui.report

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.*

class ReportFragment : Fragment() {
    private lateinit var spinnerType: Spinner
    private lateinit var editDescription: EditText
    private lateinit var recyclerImages: RecyclerView
    private lateinit var btnAddPhoto: Button
    private lateinit var switchUrgent: com.google.android.material.switchmaterial.SwitchMaterial
    private lateinit var fabSubmit: ExtendedFloatingActionButton
    private lateinit var tvLocation: TextView
    private var miniMap: org.osmdroid.views.MapView? = null
    private var progressBar: android.widget.ProgressBar? = null
    
    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var currentLat = 0.0
    private var currentLng = 0.0
    private var currentPhotoUri: Uri? = null
    private var isSubmitting = false
    
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoUri?.let { uri ->
                if (imageUris.size < 3) {
                    imageUris.add(uri)
                    imageAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Tối đa 3 ảnh", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                if (imageUris.size < 3) {
                    imageUris.add(uri)
                    imageAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "Tối đa 3 ảnh", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private val locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            getCurrentLocation()
        } else {
            Toast.makeText(context, "Cần quyền truy cập vị trí", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openCamera()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_report, container, false)
        
        try {
            spinnerType = view.findViewById(R.id.spinnerReportType)
            editDescription = view.findViewById(R.id.etDescription)
            recyclerImages = view.findViewById(R.id.rvImages)
            btnAddPhoto = view.findViewById(R.id.btnAddImage)
            switchUrgent = view.findViewById(R.id.switchUrgent)
            fabSubmit = view.findViewById(R.id.fabSubmit)
            tvLocation = view.findViewById(R.id.tvAddress)
            miniMap = view.findViewById(R.id.miniMap)
            
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            
            setupSpinner()
            setupImageRecycler()
            setupListeners()
            setupMiniMap()
            getCurrentLocation()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khởi tạo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        
        return view
    }
    
    private fun setupMiniMap() {
        miniMap?.let { map ->
            try {
                map.setMultiTouchControls(true)
                map.controller.setZoom(15.0)
                map.controller.setCenter(org.osmdroid.util.GeoPoint(21.0285, 105.8542))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.report_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter
    }
    
    private fun setupImageRecycler() {
        imageAdapter = ImageAdapter(imageUris) { position ->
            imageUris.removeAt(position)
            imageAdapter.notifyDataSetChanged()
        }
        recyclerImages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerImages.adapter = imageAdapter
    }
    
    private fun setupListeners() {
        btnAddPhoto.setOnClickListener {
            showPhotoOptions()
        }
        
        switchUrgent.setOnCheckedChangeListener { _, isChecked ->
            fabSubmit.backgroundTintList = ContextCompat.getColorStateList(
                requireContext(),
                if (isChecked) android.R.color.holo_red_dark else R.color.green_primary
            )
        }
        
        fabSubmit.setOnClickListener {
            submitReport()
        }
    }
    
    private fun getCurrentLocation() {
        tvLocation.text = "📍 Đang lấy vị trí..."
        
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude
                        tvLocation.text = "📍 ${String.format("%.6f", currentLat)}, ${String.format("%.6f", currentLng)}"
                        
                        updateMiniMap()
                    } else {
                        // Try to request new location
                        requestNewLocation()
                    }
                }
                .addOnFailureListener { e ->
                    tvLocation.text = "📍 Lỗi GPS: ${e.localizedMessage}"
                    Toast.makeText(context, "Không thể lấy vị trí. Vui lòng bật GPS", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            tvLocation.text = "📍 Lỗi: ${e.localizedMessage}"
            e.printStackTrace()
        }
    }
    
    private fun requestNewLocation() {
        try {
            val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
                priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 5000
                fastestInterval = 2000
                numUpdates = 1
            }
            
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    object : com.google.android.gms.location.LocationCallback() {
                        override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                            result.lastLocation?.let { location ->
                                currentLat = location.latitude
                                currentLng = location.longitude
                                tvLocation.text = "📍 ${String.format("%.6f", currentLat)}, ${String.format("%.6f", currentLng)}"
                                updateMiniMap()
                            }
                        }
                    },
                    android.os.Looper.getMainLooper()
                )
            }
        } catch (e: Exception) {
            tvLocation.text = "📍 Không lấy được vị trí"
            e.printStackTrace()
        }
    }
    
    private fun updateMiniMap() {
        miniMap?.let { map ->
            try {
                val geoPoint = org.osmdroid.util.GeoPoint(currentLat, currentLng)
                map.controller.setCenter(geoPoint)
                map.controller.setZoom(16.0)
                
                map.overlays.clear()
                
                val marker = org.osmdroid.views.overlay.Marker(map)
                marker.position = geoPoint
                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM)
                marker.title = "Vị trí báo cáo"
                marker.icon = resources.getDrawable(R.drawable.ic_location, null)
                
                map.overlays.add(marker)
                map.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun showPhotoOptions() {
        val options = arrayOf("Chụp ảnh", "Chọn từ thư viện")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Thêm ảnh")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermission()
                    1 -> openGallery()
                }
            }
            .show()
    }
    
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) 
            == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    private fun openCamera() {
        val photoFile = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
        currentPhotoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
        }
        cameraLauncher.launch(intent)
    }
    
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }
    
    private fun submitReport() {
        if (isSubmitting) {
            Toast.makeText(context, "Đang xử lý, vui lòng đợi...", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val description = editDescription.text.toString().trim()
            
            if (description.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập mô tả", Toast.LENGTH_SHORT).show()
                editDescription.requestFocus()
                return
            }
            
            if (description.length < 10) {
                Toast.makeText(context, "Mô tả phải có ít nhất 10 ký tự", Toast.LENGTH_SHORT).show()
                editDescription.requestFocus()
                return
            }
            
            if (currentLat == 0.0 || currentLng == 0.0) {
                Toast.makeText(context, "Đang lấy vị trí GPS, vui lòng đợi...", Toast.LENGTH_LONG).show()
                getCurrentLocation()
                return
            }
            
            isSubmitting = true
            fabSubmit.isEnabled = false
            fabSubmit.text = "Đang gửi..."
            fabSubmit.icon = null
            
            uploadImages { imageUrls ->
                saveReport(imageUrls)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            resetSubmitButton()
            e.printStackTrace()
        }
    }
    
    private fun resetSubmitButton() {
        isSubmitting = false
        fabSubmit.isEnabled = true
        fabSubmit.text = "Gửi báo cáo"
        fabSubmit.setIconResource(android.R.drawable.ic_menu_send)
    }
    
    private fun uploadImages(callback: (List<String>) -> Unit) {
        if (imageUris.isEmpty()) {
            callback(emptyList())
            return
        }
        
        val uploadedUrls = mutableListOf<String>()
        var uploadCount = 0
        val totalImages = imageUris.size
        
        imageUris.forEachIndexed { index, uri ->
            try {
                val fileName = "report_${System.currentTimeMillis()}_$index.jpg"
                val ref = storage.reference.child("reports/$fileName")
                
                ref.putFile(uri)
                    .addOnSuccessListener { taskSnapshot ->
                        ref.downloadUrl.addOnSuccessListener { downloadUri ->
                            uploadedUrls.add(downloadUri.toString())
                            uploadCount++
                            
                            // Update progress
                            activity?.runOnUiThread {
                                fabSubmit.text = "Đang tải ảnh $uploadCount/$totalImages..."
                            }
                            
                            if (uploadCount == totalImages) {
                                callback(uploadedUrls)
                            }
                        }.addOnFailureListener { e ->
                            uploadCount++
                            e.printStackTrace()
                            if (uploadCount == totalImages) {
                                callback(uploadedUrls)
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        uploadCount++
                        e.printStackTrace()
                        Toast.makeText(context, "Lỗi tải ảnh ${index + 1}", Toast.LENGTH_SHORT).show()
                        if (uploadCount == totalImages) {
                            callback(uploadedUrls)
                        }
                    }
            } catch (e: Exception) {
                uploadCount++
                e.printStackTrace()
                if (uploadCount == totalImages) {
                    callback(uploadedUrls)
                }
            }
        }
    }
    
    private fun saveReport(imageUrls: List<String>) {
        try {
            val user = auth.currentUser
            if (user == null) {
                Toast.makeText(context, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
                resetSubmitButton()
                return
            }
            
            val reportId = db.collection("reports").document().id
            
            val report = hashMapOf(
                "reportId" to reportId,
                "userId" to user.uid,
                "userName" to (user.displayName ?: user.email ?: "User"),
                "location" to hashMapOf(
                    "lat" to currentLat,
                    "lng" to currentLng
                ),
                "type" to spinnerType.selectedItem.toString(),
                "description" to editDescription.text.toString().trim(),
                "images" to imageUrls,
                "isUrgent" to switchUrgent.isChecked,
                "status" to "pending",
                "createdAt" to System.currentTimeMillis(),
                "address" to ""
            )
            
            db.collection("reports").document(reportId)
                .set(report)
                .addOnSuccessListener {
                    resetSubmitButton()
                    Toast.makeText(context, "✅ Gửi báo cáo thành công!", Toast.LENGTH_LONG).show()
                    
                    // Clear form
                    editDescription.text?.clear()
                    imageUris.clear()
                    imageAdapter.notifyDataSetChanged()
                    switchUrgent.isChecked = false
                    spinnerType.setSelection(0)
                    
                    // Scroll to top
                    view?.findViewById<androidx.core.widget.NestedScrollView>(R.id.scrollView)?.smoothScrollTo(0, 0)
                }
                .addOnFailureListener { e ->
                    resetSubmitButton()
                    Toast.makeText(context, "❌ Lỗi kết nối: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
        } catch (e: Exception) {
            resetSubmitButton()
            Toast.makeText(context, "Lỗi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    override fun onResume() {
        super.onResume()
        miniMap?.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        miniMap?.onPause()
    }
}
