package com.example.firebasesample

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_history.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var db : FirebaseFirestore
    private lateinit var historyAdapter: HistoryAdapter

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        db = FirebaseFirestore.getInstance()
        val employeeId = intent.getStringExtra("employeeId")
        Log.d("EmployeeId", employeeId)
        historyAdapter = HistoryAdapter({
            db.collection("Checkin").whereEqualTo("employeeId", employeeId).orderBy("eventId")
        })

        historyView.also {recyclerView: RecyclerView ->
            recyclerView.adapter = historyAdapter
            recyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    override fun onStart() {
        super.onStart()
        historyAdapter.clear()
        historyAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        historyAdapter.stopListening()
    }
}