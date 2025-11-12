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
        holder.tvStatus.text = when (report.status) {
            "pending" -> "⏳ Chờ xử lý"
            "assigned" -> "👷 Đã phân công"
            "processing" -> "🔧 Đang xử lý"
            "done" -> "✅ Hoàn thành"
            else -> report.status
        }
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(Date(report.createdAt))
        
        holder.itemView.setOnClickListener {
            onClick(report)
        }
    }

    override fun getItemCount() = reports.size
}
