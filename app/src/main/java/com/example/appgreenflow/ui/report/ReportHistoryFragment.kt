package com.example.appgreenflow.ui.report

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReportHistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var adapter: ReportHistoryAdapter
    private val reports = mutableListOf<Report>()
    
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_report_history, container, false)
        
        try {
            recyclerView = view.findViewById(R.id.rvReports)
            tvEmpty = view.findViewById(R.id.tvEmpty)
            progressBar = view.findViewById(R.id.progressBar)
            
            setupRecyclerView()
            loadReports()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi khởi tạo: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
        
        return view
    }
    
    private fun setupRecyclerView() {
        adapter = ReportHistoryAdapter(reports) { report ->
            openReportDetail(report)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun openReportDetail(report: Report) {
        try {
            val fragment = ReportDetailFragment.newInstance(report.reportId)
            parentFragmentManager.beginTransaction()
                .replace(R.id.nav_home, fragment)
                .addToBackStack(null)
                .commit()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi mở chi tiết", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    
    private fun loadReports() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
            tvEmpty.visibility = View.VISIBLE
            tvEmpty.text = "Vui lòng đăng nhập để xem lịch sử"
            return
        }
        
        progressBar.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE
        
        db.collection("reports")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                progressBar.visibility = View.GONE
                
                if (error != null) {
                    Toast.makeText(context, "Lỗi tải dữ liệu: ${error.message}", Toast.LENGTH_SHORT).show()
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Lỗi tải dữ liệu"
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                
                reports.clear()
                snapshot?.documents?.forEach { doc ->
                    try {
                        doc.toObject(Report::class.java)?.let { reports.add(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                adapter.notifyDataSetChanged()
                
                if (reports.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    tvEmpty.text = "Chưa có báo cáo nào"
                } else {
                    tvEmpty.visibility = View.GONE
                }
            }
    }
}
