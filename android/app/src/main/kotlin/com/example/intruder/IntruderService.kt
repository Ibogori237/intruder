package com.example.intruder

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IntruderService : DeviceAdminReceiver() {

    override fun onPasswordFailed(context: Context, intent: Intent) {
        Log.d("IntruderService", "Password failed detected.")
        Toast.makeText(context, "Échec de tentative de déverrouillage détecté", Toast.LENGTH_SHORT).show()

        try {
            Log.d("IntruderService", "Initiating photo capture...")
            PhotoCapture.takePhoto(context) { photoPath, locationLink ->
                if (photoPath.isNotEmpty() && locationLink.isNotEmpty()) {
                    Log.d("IntruderService", "Photo saved at: $photoPath, Location: $locationLink")
                    try {
                        Log.d("IntruderService", "Sending email...")
                        CoroutineScope(Dispatchers.IO).launch {
                            // Use a default recipient email here, or fetch from a config if needed
                            val recipientEmail = "ibrahimabakargori235@gmail.com"
                            EmailSender.sendEmail(locationLink, photoPath, recipientEmail)
                            Log.d("IntruderService", "Email sent.")
                        }
                    } catch (e: Exception) {
                        Log.e("IntruderService", "Email sending failed: ${e.message}", e)
                    }
                } else {
                    Log.e("IntruderService", "Photo capture or location retrieval failed.")
                }
            }
        } catch (e: Exception) {
            Log.e("IntruderService", "Photo capture failed: ${e.message}", e)
        }
    }

    companion object {
        fun requestDeviceAdmin(activity: android.app.Activity) {
            val componentName = ComponentName(activity, IntruderService::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Autoriser Intruder à surveiller les tentatives.")
            }
            activity.startActivityForResult(intent, 1)
        }
    }
}