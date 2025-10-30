package com.example.appgreenflow.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.appgreenflow.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SupportFragment : Fragment() {
    private var spinnerIssue: Spinner? = null
    private var etDesc: TextInputEditText? = null
    private var btnSubmit: Button? = null
    private var db: FirebaseFirestore? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_support, container, false)
        db = FirebaseFirestore.getInstance()
        spinnerIssue = view.findViewById<Spinner>(R.id.spinnerIssue)
        etDesc = view.findViewById<TextInputEditText>(R.id.etDesc)
        btnSubmit = view.findViewById<Button>(R.id.btnSubmitSupport)

        setupSpinner()
        btnSubmit!!.setOnClickListener(View.OnClickListener { v: View? -> submitReport() })
        return view
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.support_issues,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerIssue!!.setAdapter(adapter)
    }

    private fun submitReport() {
        val issue = spinnerIssue!!.getSelectedItem().toString()
        val desc = etDesc!!.getText().toString().trim { it <= ' ' }
        if (desc.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng mô tả vấn đề!", Toast.LENGTH_SHORT).show()
            return
        }

        val report: MutableMap<String?, Any?> = HashMap<String?, Any?>()
        report.put("issue", issue)
        report.put("desc", desc)
        report.put("userId", FirebaseAuth.getInstance().getUid())
        report.put("timestamp", System.currentTimeMillis())

        db!!.collection("support_reports").add(report)
            .addOnSuccessListener(OnSuccessListener { aVoid: DocumentReference? ->
                Toast.makeText(getContext(), "Báo cáo đã gửi thành công!", Toast.LENGTH_SHORT)
                    .show()
                etDesc!!.setText("")
            })
            .addOnFailureListener(OnFailureListener { e: Exception? ->
                Toast.makeText(
                    getContext(),
                    "Lỗi gửi: " + e!!.message,
                    Toast.LENGTH_SHORT
                ).show()
            })
    }
}