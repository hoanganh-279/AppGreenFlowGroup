package com.example.appgreenflow.ui.report

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReportUtils {
    
    fun getStatusColor(status: String): Int {
        return when (status) {
            "pending" -> android.graphics.Color.parseColor("#FFA500")
            "processing" -> android.graphics.Color.parseColor("#2196F3")
            "resolved" -> android.graphics.Color.parseColor("#4CAF50")
            "rejected" -> android.graphics.Color.parseColor("#F44336")
            else -> android.graphics.Color.GRAY
        }
    }
    
    fun getStatusText(status: String): String {
        return when (status) {
            "pending" -> "Chờ xử lý"
            "processing" -> "Đang xử lý"
            "resolved" -> "Đã giải quyết"
            "rejected" -> "Từ chối"
            else -> "Không xác định"
        }
    }
    
    fun getPriorityText(isUrgent: Boolean): String {
        return if (isUrgent) "Khẩn cấp" else "Bình thường"
    }
    
    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    fun compressImage(context: Context, uri: Uri, maxSizeKB: Int = 500): String? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            var quality = 90
            var outputStream: ByteArrayOutputStream
            
            do {
                outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                quality -= 10
            } while (outputStream.size() > maxSizeKB * 1024 && quality > 10)
            
            val byteArray = outputStream.toByteArray()
            return Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
