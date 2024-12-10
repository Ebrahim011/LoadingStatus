package com.ebrahimamin.loadingstatusbaranimation

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private var downloadID: Long = 0

    private lateinit var loadingButton: LoadingButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var glideRadio: RadioButton
    private lateinit var loadAppRadio: RadioButton
    private lateinit var retrofitRadio: RadioButton

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private var url: String? = null

    private var radioSelection: RadioSelection? = null
    private var downloadedUrl: String? = null
    private var downloadStatus: String? = null

    enum class RadioSelection {
        GLIDE, LOADAPP, RETROFIT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingButton = findViewById(R.id.custom_button)
        radioGroup = findViewById(R.id.radio_group)
        glideRadio = findViewById(R.id.glide_radio)
        loadAppRadio = findViewById(R.id.load_app_radio)
        retrofitRadio = findViewById(R.id.retrofit_radio)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                sendNotification("Download complete", this)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        createNotificationChannel("download_channel", "Download Notifications")

        loadingButton.setOnClickListener {
            loadingButton.buttonState = ButtonState.Clicked
            if (radioGroup.checkedRadioButtonId == -1) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            } else {
                loadingButton.buttonState = ButtonState.Loading
                url = when (radioGroup.checkedRadioButtonId) {
                    R.id.glide_radio -> "https://github.com/bumptech/glide/archive/master.zip"
                    R.id.load_app_radio -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
                    R.id.retrofit_radio -> "https://github.com/square/retrofit/archive/master.zip"
                    else -> null
                }
                radioGroup.clearCheck()
                download(url)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            downloadStatus = if (id == downloadID) "Download successful" else "Download failed"
            loadingButton.buttonState = ButtonState.Completed
            downloadedUrl = url
            prepareNotification(context)
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                sendNotification("Download complete", context)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun prepareNotification(context: Context?) {
        val intentToDetailActivity = Intent(context, DetailActivity::class.java).apply {
            putExtra("downloadedUrl", downloadedUrl)
            putExtra("downloadStatus", downloadStatus)
        }
        pendingIntent = PendingIntent.getActivity(
            context, 0, intentToDetailActivity, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        action = NotificationCompat.Action(R.drawable.ic_assistant_black_24dp, "View Details", pendingIntent)
    }

    private fun sendNotification(messageBody: String, context: Context) {
        val builder = NotificationCompat.Builder(context, "download_channel")
            .setContentTitle("Download Notification")
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentText(messageBody)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(action)
        notificationManager.notify(0, builder.build())
    }

    private fun createNotificationChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                description = "Channel for download notifications"
            }
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun download(url: String?) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading")
            .setDescription("Downloading file")
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID = downloadManager.enqueue(request)
    }
}