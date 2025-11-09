# Bảng Màu GreenFlow - Ứng Dụng Thu Gom Rác Thải

## 🎨 Màu Chủ Đạo

### Xanh Lá Môi Trường
- **Primary Green** `#00C853` - Màu xanh lá chính, tươi sáng, thể hiện sự sống và môi trường
- **Primary Dark** `#00A344` - Xanh lá đậm cho status bar và các phần tử nổi bật
- **Primary Light** `#5EFC82` - Xanh lá nhạt cho highlight và hover states

### Màu Phụ
- **Accent Green** `#76FF03` - Xanh neon cho các điểm nhấn quan trọng
- **Secondary Green** `#4CAF50` - Xanh lá phụ cho các thành phần bổ sung

## 🎯 Ứng Dụng Màu Sắc

### Toolbar & Navigation
- Background: `#00C853` (Primary Green)
- Text/Icons: `#FFFFFF` (White)
- Status Bar: `#00A344` (Primary Dark)

### Buttons
- Primary Button: Gradient `#00C853` → `#00A344`
- Secondary Button: Border `#00C853` với nền trong suốt
- FAB (Chat): `#00C853` với icon trắng

### Backgrounds
- Main Background: `#F1F8F4` (Xanh nhạt nhẹ nhàng)
- Card Background: `#FAFFFE` (Trắng kem)
- White: `#FFFFFF`

### Text Colors
- Primary Text: `#1B5E20` (Xanh đậm dễ đọc)
- Secondary Text: `#388E3C` (Xanh vừa)
- Hint Text: `#81C784` (Xanh nhạt)

### Status Colors
- ✅ Success: `#00C853` (Xanh)
- ⚠️ Warning: `#FF9800` (Cam)
- ❌ Error: `#F44336` (Đỏ)
- ℹ️ Info: `#2196F3` (Xanh dương)

### Trash Status
- 🔴 Full (≥90%): `#F44336` (Đỏ)
- 🟠 Half (70-89%): `#FF9800` (Cam)
- 🟢 Empty (<70%): `#4CAF50` (Xanh)

## 💬 Chat Messages
- Sent Message: `#C8E6C9` (Xanh nhạt)
- Received Message: `#EEEEEE` (Xám nhạt)

## 🎭 Triết Lý Thiết Kế

Bảng màu được thiết kế dựa trên:
1. **Xanh lá cây** - Biểu tượng của thiên nhiên, môi trường và sự sống
2. **Tươi sáng** - Tạo cảm giác tích cực, năng động
3. **Dễ nhìn** - Tương phản tốt, dễ đọc trong mọi điều kiện ánh sáng
4. **Chuyên nghiệp** - Phù hợp với ứng dụng dịch vụ công

## 🔧 Sử dụng

Tất cả màu đã được định nghĩa trong:
- `app/src/main/res/values/colors.xml`
- `app/src/main/res/values/themes.xml`

Sử dụng trong XML:
```xml
android:background="@color/green_primary"
android:textColor="@color/text_primary"
```

Sử dụng trong Kotlin:
```kotlin
getColor(R.color.green_primary)
```
