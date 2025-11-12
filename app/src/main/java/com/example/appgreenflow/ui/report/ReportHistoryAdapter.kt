package com.example.appgreenflow.ui.report

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appgreenflow.R
import java.text.SimpleDateFormat
import java.util.*

class ReportHistoryAdapter(
    private val reports: List<Report>,
    private val onClick: (Report) -> Unit
) : RecyclerView.Adapter<ReportHistoryAdapter.ViewHolder>() {

    // Tối ưu: Tạo DateFormat 1 lần duy nhất
    companion object {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tvType)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val report = reports[position]
        
        holder.tvType.text = report.type
        holder.tvDescription.text = report.description
        
        // Tối ưu: Cache status text
        holder.tvStatus.text = getStatusText(report.status)
        
        // Dùng dateFormat đã tạo sẵn
        holder.tvDate.text = dateFormat.format(Date(report.createdAt))
        
        holder.itemView.setOnClickListener {
            onClick(report)
        }
    }

    override fun getItemCount() = reports.size
    
    // Tối ưu: Tách logic status ra method riêng
    private fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "⏳ Chờ xử lý"
            "assigned" -> "👷 Đã phân công"
            "processing" -> "🔧 Đang xử lý"
            "done" -> "✅ Hoàn thành"
            else -> status
        }
    }
}
