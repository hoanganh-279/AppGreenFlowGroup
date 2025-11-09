package com.example.appgreenflow

import android.app.Activity
import android.content.Intent
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

object ChatHelper {
    
    fun addChatButton(activity: Activity) {
        // Kiểm tra xem button đã tồn tại chưa
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        if (rootView.findViewById<FloatingActionButton>(R.id.fabChat) != null) {
            return // Button đã tồn tại, không thêm nữa
        }
        
        val fabChat = FloatingActionButton(activity).apply {
            id = R.id.fabChat
            setImageResource(R.drawable.ic_chat)
            size = FloatingActionButton.SIZE_NORMAL
            elevation = 6f
            backgroundTintList = android.content.res.ColorStateList.valueOf(
                activity.getColor(R.color.green_primary)
            )
            imageTintList = android.content.res.ColorStateList.valueOf(
                activity.getColor(R.color.white)
            )
            
            setOnClickListener {
                // Không mở ChatActivity nếu đang ở trong ChatActivity
                if (activity !is ChatActivity) {
                    val intent = Intent(activity, ChatActivity::class.java)
                    activity.startActivity(intent)
                }
            }
        }
        
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = android.view.Gravity.BOTTOM or android.view.Gravity.END
            setMargins(0, 0, 48, 48)
        }
        
        rootView.addView(fabChat, params)
    }
}
