package com.ebrahimamin.loadingstatusbaranimation

import android.app.NotificationManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    private lateinit var fileNameTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var confirmButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        fileNameTextView = findViewById(R.id.repository_name_text_view)
        statusTextView = findViewById(R.id.download_status_text_view)
        confirmButton = findViewById(R.id.confirm_button)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancelAll()

        fileNameTextView.text = intent.getStringExtra("downloadedUrl")
        statusTextView.text = intent.getStringExtra("downloadStatus")

        confirmButton.setOnClickListener { finish() }
    }
}