package com.example.firebasesample

import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {

    lateinit var db : FirebaseFirestore
    lateinit var prefs : SharedPreferences
    lateinit var employeeId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar.visibility = android.widget.ProgressBar.INVISIBLE
        db = FirebaseFirestore.getInstance()
        prefs = getSharedPreferences(getString(R.string.preferences_key), AppCompatActivity.MODE_PRIVATE)
        employeeId = prefs.getString(getString(R.string.preferences_employee), "")
        editText.setText(employeeId, TextView.BufferType.NORMAL)
        // チェックイン
        add.setOnClickListener {
            employeeId = editText.text.toString()
            if (employeeId.isEmpty()) {
                editText.setError("社員番号を入力してください")
            } else {
                val e : SharedPreferences.Editor = prefs.edit()
                e.putString(getString(R.string.preferences_employee) , employeeId)
                e.apply()
                IntentIntegrator(this).initiateScan()
            }
        }
        // 履歴表示
        get.setOnClickListener {
            employeeId = editText.text.toString()
            val intent = Intent(this, HistoryActivity::class.java).apply {
                putExtra("employeeId", employeeId)
            }
            startActivity(intent)
        }
        // 今月の蕎麦
        soba.setOnClickListener {
            val df = SimpleDateFormat("yyyyMM")
            val month = df.format(Date()).toInt()
            Log.d("month", month.toString())
            db.collection("Checkin")
                .whereEqualTo("eventMonth", month)
                .whereEqualTo("sobaJoin", 0)
                .get()
                .addOnCompleteListener{
                    if (it.isSuccessful) {
                        val checkins = it.result?.toObjects(Checkin::class.java)
                        val count = checkins?.size ?: 0
                        var employeeIds: ArrayList<String> = arrayListOf()
                        checkins?.forEach {
                            Log.d("employee_id", it.employeeId)
                            employeeIds.add(it.employeeId)
                        }
                        AlertDialog.Builder(this).apply {
                            setTitle(month.toString() + "の蕎麦人数：" + count.toString())
                            setMessage(employeeIds.toString())
                            setPositiveButton("はい", DialogInterface.OnClickListener { _, _ -> })
                        }.show()
                    } else {
                        Log.d("Firestore", "failure")
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            val scanData = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            scanData.contents?.let {
                // QR読み込み(ex. {"event_id":1, "event_name":"WM&P部門会議", "event_month":201909})
                val jsonData = JSONObject(scanData.contents)
                val eventId = jsonData.getInt("event_id")
                val eventName = jsonData.getString("event_name")
                val eventMonth = jsonData.getInt("event_month")
                employeeId = prefs.getString(getString(R.string.preferences_employee), "")
                // 重複チェック
                progressBar.visibility = android.widget.ProgressBar.VISIBLE
                db.collection("Checkin")
                    .whereEqualTo("employeeId", employeeId)
                    .whereEqualTo("eventId", eventId)
                    .get()
                    .addOnCompleteListener{
                        if (it.isSuccessful) {
                            progressBar.visibility = android.widget.ProgressBar.INVISIBLE
                            if (it.result?.toObjects(Checkin::class.java)?.size!! > 0) {
                                showDuplicateDialog(eventName, eventMonth)
                            } else {
                                showCheckinDialog(eventName, eventId, eventMonth)
                            }
                        } else {
                            Log.d("Firestore", "failure")
                        }
                    }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDuplicateDialog(eventName: String, eventMonth: Int) {
        AlertDialog.Builder(this).apply {
            setTitle("社員番号：" + employeeId + "\n" + "イベント：" + eventMonth.toString() + eventName)
            setMessage("既にチェックイン済みです")
            setPositiveButton("はい", DialogInterface.OnClickListener { _, _ -> })
        }.show()
    }
    private fun showCheckinDialog(eventName: String, eventId: Int, eventMonth: Int) {
        var selectedId = 0
        val sobaJoin = arrayOf<String>("蕎麦も行く", "今回は大変残念だが辞めておく")
        AlertDialog.Builder(this).apply {
            setTitle("社員番号：" + employeeId + "\n" + "イベント：" + eventMonth.toString() + eventName)
            setSingleChoiceItems(sobaJoin, -1) { _, i ->
                selectedId = i
            }
            setPositiveButton("チェックイン", DialogInterface.OnClickListener { _, _ ->
                val checkin = Checkin(employeeId, eventId, eventName, selectedId, eventMonth)
                db.collection("Checkin")
                    .document()
                    .set(checkin)
                    .addOnCompleteListener { Log.d("firestore", "complete") }
                    .addOnCanceledListener { Log.d("firestore", "cansel") }
                    .addOnSuccessListener { Log.d("firestore", "success") }
                    .addOnFailureListener { Log.d("firestore", "failure") }
                Toast.makeText(context, "チェックインしました", Toast.LENGTH_SHORT).show()
            })
            setNegativeButton("キャンセル", null)
        }.show()
    }
}
