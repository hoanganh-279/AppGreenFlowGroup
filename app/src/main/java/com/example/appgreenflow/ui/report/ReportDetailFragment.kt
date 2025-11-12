package com.example.appgreenflow.ui.report

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R
import com.google.firebase.firestore.FirebaseFirestore

class ReportDetailFragment : Fragment() {
    private lateinit var tvType: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvPriority: TextView
    private lateinit var rvImages: RecyclerView
    private lateinit var progressBar: ProgressBar
    
    private val db = FirebaseFirestore.getInstance()
    private var reportId: String? = null

    companion object {
        private const val ARG_REPORT_ID = "report_id"
        
        fun newInstance(reportId: String): ReportDetailFragment {
            return ReportDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REPORT_ID, reportId)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_report_detail, container, false)
        
        try {
            tvType = view.findViewById(R.id.tvType)
            tvDescription = view.findViewById(R.id.tvDescription)
            tvStatus = view.findViewById(R.id.tvStatus)
            tvDate = view.findViewById(R.id.tvDate)
            tvLocation = view.findViewById(R.id.tvLocation)
            tvPriority = view.findViewById(R.id.tvPriority)
            rvImages = view.findViewById(R.id.rvImages)
            progressBar = view.findViewById(R.id.progressBar)
            
            rvImages.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            
            reportId = arguments?.getString(ARG_REPORT_ID)
            if (reportId != null) {
                loadReportDetail(reportId!!)
            } else {
                Toast.makeText(context, "Lỗi: Không tìm thấy báo cáo", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khởi tạo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        
        return view
    }
    
    private fun loadReportDetail(reportId: String) {
        progressBar.visibility = View.VISIBLE
        
        db.collection("reports").document(reportId)
            .get()
            .addOnSuccessListener { document ->
                progressBar.visibility = View.GONE
                
                if (document.exists()) {
                    try {
                        val report = document.toObject(Report::class.java)
                        report?.let { displayReport(it) }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Lỗi đọc dữ liệu", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(context, "Không tìm thấy báo cáo", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Lỗi tải dữ liệu: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }
    
    private fun displayReport(report: Report) {
        tvType.text = report.type
        tvDescription.text = report.description
        tvDate.text = ReportUtils.formatDate(report.createdAt)
        tvLocation.text = "📍 ${String.format("%.6f", report.location.lat)}, ${String.format("%.6f", report.location.lng)}"
        
        // Status
        tvStatus.text = ReportUtils.getStatusText(report.status)
        tvStatus.setTextColor(ReportUtils.getStatusColor(report.status))
        
        // Priority
        tvPriority.text = ReportUtils.getPriorityText(report.isUrgent)
        tvPriority.setTextColor(if (report.isUrgent) Color.RED else Color.GRAY)
        
        // Images
        if (report.images.isNotEmpty()) {
            val adapter = ReportImageDetailAdapter(report.images)
            rvImages.adapter = adapter
            rvImages.visibility = View.VISIBLE
        } else {
            rvImages.visibility = View.GONE
        }
    }
}
