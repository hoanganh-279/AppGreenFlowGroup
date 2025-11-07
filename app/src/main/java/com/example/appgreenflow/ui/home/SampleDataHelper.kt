package com.example.appgreenflow.ui.home

import com.google.firebase.firestore.FirebaseFirestore

object SampleDataHelper {
    
    fun addSampleArticles() {
        val db = FirebaseFirestore.getInstance()
        
        val sampleArticles = listOf(
            mapOf(
                "title" to "Tác động của rác thải nhựa đến môi trường",
                "desc" to "Rác thải nhựa đang gây ra những tác động nghiêm trọng đến hệ sinh thái biển và đất liền. Hãy cùng tìm hiểu về vấn đề này.",
                "content" to "Rác thải nhựa là một trong những vấn đề môi trường nghiêm trọng nhất hiện nay. Mỗi năm, hàng triệu tấn rác thải nhựa được thải ra môi trường, gây ô nhiễm đại dương, đất và không khí.\n\nTác động đến sinh vật biển:\n- Nhiều loài động vật biển nhầm lẫn rác thải nhựa với thức ăn\n- Rác thải nhựa có thể làm tắc nghẽn đường tiêu hóa của động vật\n- Microplastic xâm nhập vào chuỗi thức ăn\n\nTác động đến con người:\n- Microplastic có thể xuất hiện trong nước uống và thực phẩm\n- Ô nhiễm không khí từ việc đốt rác thải nhựa\n- Chi phí xử lý rác thải ngày càng tăng cao\n\nGiải pháp:\n- Giảm sử dụng nhựa một lần\n- Tái chế và tái sử dụng\n- Sử dụng các vật liệu thay thế thân thiện với môi trường",
                "imageUrl" to "https://images.unsplash.com/photo-1583212292454-1fe6229603b7?w=500",
                "timestamp" to System.currentTimeMillis()
            ),
            mapOf(
                "title" to "Hướng dẫn phân loại rác tại nhà",
                "desc" to "Phân loại rác đúng cách là bước đầu tiên để bảo vệ môi trường. Hãy học cách phân loại rác hiệu quả ngay tại nhà.",
                "content" to "Phân loại rác tại nhà là một thói quen tốt mà mọi gia đình nên thực hiện để góp phần bảo vệ môi trường.\n\nCác loại rác cần phân loại:\n\n1. Rác hữu cơ (màu xanh lá):\n- Thức ăn thừa, vỏ trái cây\n- Lá cây, cành cây nhỏ\n- Giấy ăn đã sử dụng\n\n2. Rác tái chế (màu xanh dương):\n- Chai nhựa, lon nhôm\n- Giấy, carton sạch\n- Túi nilon sạch\n\n3. Rác không tái chế (màu đỏ):\n- Bao bì nhiều lớp\n- Đồ điện tử hỏng\n- Giấy ăn bẩn\n\nLợi ích của việc phân loại rác:\n- Giảm lượng rác thải ra môi trường\n- Tăng hiệu quả tái chế\n- Tiết kiệm chi phí xử lý rác\n- Tạo nguyên liệu cho sản xuất mới\n\nMẹo thực hiện:\n- Chuẩn bị 3 thùng rác có màu khác nhau\n- Dán nhãn rõ ràng trên mỗi thùng\n- Rửa sạch bao bì trước khi bỏ vào thùng tái chế",
                "imageUrl" to "https://images.unsplash.com/photo-1532996122724-e3c354a0b15b?w=500",
                "timestamp" to System.currentTimeMillis() - 86400000 // 1 ngày trước
            ),
            mapOf(
                "title" to "Lợi ích của việc tái chế",
                "desc" to "Tái chế không chỉ giúp bảo vệ môi trường mà còn mang lại nhiều lợi ích kinh tế và xã hội. Khám phá những lợi ích tuyệt vời này.",
                "content" to "Tái chế là quá trình chuyển đổi rác thải thành nguyên liệu mới để sản xuất các sản phẩm khác. Đây là một trong những cách hiệu quả nhất để bảo vệ môi trường.\n\nLợi ích môi trường:\n- Giảm lượng rác thải chôn lấp\n- Tiết kiệm tài nguyên thiên nhiên\n- Giảm ô nhiễm không khí và nước\n- Bảo vệ hệ sinh thái tự nhiên\n\nLợi ích kinh tế:\n- Tạo việc làm trong ngành tái chế\n- Tiết kiệm chi phí sản xuất\n- Giảm chi phí xử lý rác thải\n- Tạo ra các sản phẩm có giá trị\n\nLợi ích xã hội:\n- Nâng cao ý thức bảo vệ môi trường\n- Tạo thói quen tốt cho thế hệ trẻ\n- Xây dựng cộng đồng bền vững\n- Cải thiện chất lượng cuộc sống\n\nCác vật liệu có thể tái chế:\n- Giấy và carton: 80-90% có thể tái chế\n- Nhựa: Tùy loại, từ 20-90%\n- Kim loại: Gần như 100%\n- Thủy tinh: 100% có thể tái chế vô số lần\n\nHãy bắt đầu tái chế ngay hôm nay để góp phần xây dựng một tương lai xanh!",
                "imageUrl" to "https://images.unsplash.com/photo-1542601906990-b4d3fb778b09?w=500",
                "timestamp" to System.currentTimeMillis() - 172800000 // 2 ngày trước
            )
        )
        
        sampleArticles.forEach { article ->
            db.collection("articles")
                .add(article)
                .addOnSuccessListener {
                    println("Đã thêm bài báo: ${article["title"]}")
                }
                .addOnFailureListener { e ->
                    println("Lỗi thêm bài báo: ${e.message}")
                }
        }
    }
}