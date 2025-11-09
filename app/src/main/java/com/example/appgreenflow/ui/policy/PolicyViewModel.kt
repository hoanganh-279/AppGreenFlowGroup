package com.example.appgreenflow.ui.policy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class PolicyViewModel : ViewModel() {
    private val policyText = MutableLiveData<String?>()
    private val db: FirebaseFirestore

    init {
        db = FirebaseFirestore.getInstance()
    }

    fun loadPolicy(role: String) {
        policyText.setValue("Đang tải chính sách...")

        db.collection("policies").document(role)
            .get()
            .addOnSuccessListener(OnSuccessListener { documentSnapshot: DocumentSnapshot? ->
                if (documentSnapshot!!.exists()) {
                    val text = documentSnapshot.getString("text")
                    policyText.setValue(if (text != null) text else getDefaultPolicy(role))
                } else {
                    // Tạo chính sách mặc định nếu chưa có
                    val defaultPolicy = getDefaultPolicy(role)
                    policyText.setValue(defaultPolicy)
                    
                    // Lưu vào Firestore để lần sau không cần tạo lại
                    val policyData = hashMapOf("text" to defaultPolicy)
                    db.collection("policies").document(role).set(policyData)
                }
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                // Nếu lỗi, hiển thị chính sách mặc định
                policyText.setValue(getDefaultPolicy(role))
            })
    }
    
    private fun getDefaultPolicy(role: String): String {
        return when (role) {
            "customer" -> """
                CHÍNH SÁCH SỬ DỤNG DỊCH VỤ - KHÁCH HÀNG
                
                1. GIỚI THIỆU
                Chào mừng bạn đến với GreenFlow - Hệ thống quản lý rác thải thông minh. Bằng việc sử dụng ứng dụng này, bạn đồng ý tuân thủ các điều khoản và chính sách sau.
                
                2. QUYỀN VÀ TRÁCH NHIỆM CỦA KHÁCH HÀNG
                - Được thông báo về tình trạng thùng rác trong khu vực
                - Được báo cáo các vấn đề về thùng rác (đầy, hư hỏng, mất vệ sinh)
                - Được xem lộ trình thu gom rác
                - Được hỗ trợ qua chat trực tuyến
                - Phải cung cấp thông tin chính xác khi báo cáo
                - Không spam hoặc lạm dụng hệ thống
                
                3. BẢO MẬT THÔNG TIN
                - Thông tin cá nhân của bạn được bảo mật theo quy định
                - Chúng tôi chỉ sử dụng thông tin để cải thiện dịch vụ
                - Không chia sẻ thông tin cho bên thứ ba
                
                4. ĐIỀU KHOẢN SỬ DỤNG
                - Không sử dụng ứng dụng cho mục đích bất hợp pháp
                - Không can thiệp vào hoạt động của hệ thống
                - Tuân thủ quy định về phân loại rác thải
                
                5. LIÊN HỆ HỖ TRỢ
                Nếu có thắc mắc, vui lòng liên hệ qua:
                - Chat trong ứng dụng
                - Email: support@greenflow.vn
                - Hotline: 1900-xxxx
                
                Cập nhật lần cuối: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())}
            """.trimIndent()
            
            "employee" -> """
                CHÍNH SÁCH LÀM VIỆC - NHÂN VIÊN
                
                1. GIỚI THIỆU
                Chào mừng bạn đến với GreenFlow. Là nhân viên, bạn có trách nhiệm đảm bảo hệ thống thu gom rác hoạt động hiệu quả.
                
                2. QUYỀN VÀ TRÁCH NHIỆM CỦA NHÂN VIÊN
                - Được xem tất cả thông báo về thùng rác đầy
                - Được xem và xử lý báo cáo từ khách hàng
                - Được hỗ trợ qua chat với khách hàng
                - Phải xác nhận khi đã thu gom rác
                - Phải xử lý báo cáo kịp thời
                - Phải cập nhật trạng thái công việc chính xác
                
                3. QUY TRÌNH LÀM VIỆC
                - Kiểm tra thông báo thùng rác đầy hàng ngày
                - Ưu tiên thùng rác có mức độ đầy cao (>90%)
                - Xác nhận thu gom sau khi hoàn thành
                - Báo cáo các vấn đề phát sinh
                
                4. AN TOÀN LAO ĐỘNG
                - Tuân thủ quy định an toàn lao động
                - Sử dụng đầy đủ trang thiết bị bảo hộ
                - Báo cáo ngay khi có sự cố
                
                5. BẢO MẬT THÔNG TIN
                - Không tiết lộ thông tin khách hàng
                - Không sử dụng thông tin cho mục đích cá nhân
                - Bảo mật tài khoản đăng nhập
                
                6. KỶ LUẬT
                - Vi phạm chính sách sẽ bị xử lý theo quy định
                - Nghiêm cấm lạm dụng quyền hạn
                
                Cập nhật lần cuối: ${java.text.SimpleDateFormat("dd/MM/yyyy").format(java.util.Date())}
            """.trimIndent()
            
            else -> """
                CHÍNH SÁCH SỬ DỤNG
                
                Chính sách cho vai trò "$role" chưa được thiết lập.
                Vui lòng liên hệ quản trị viên để biết thêm chi tiết.
                
                Email: support@greenflow.vn
                Hotline: 1900-xxxx
            """.trimIndent()
        }
    }

    fun getPolicyText(): LiveData<String?> {
        return policyText
    }

    fun updatePolicyText(newText: String?) {
        policyText.setValue(newText)
    }
}