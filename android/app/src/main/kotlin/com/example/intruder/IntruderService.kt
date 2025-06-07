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
            PhotoCapture.takePhoto(context)
        } catch (e: Exception) {
            Log.e("IntruderService", "Photo capture failed: ${e.message}", e)
        }

        try {
            Log.d("IntruderService", "Requesting location...")
            LocationHelper.getLocation(context) { location ->
                Log.d("IntruderService", "Location obtained: $location")
                try {
                    Log.d("IntruderService", "Sending email...")
                    CoroutineScope(Dispatchers.IO).launch {
                        // Je suppose que EmailSender.sendEmail(to, location) prend un String en premier paramètre
                        // Met un email valide ou ce qui est attendu comme 1er paramètre
                        EmailSender.sendEmail("your_email@example.com", location)
                        Log.d("IntruderService", "Email sent.")
                    }
                } catch (e: Exception) {
                    Log.e("IntruderService", "Email sending failed: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("IntruderService", "Location retrieval failed: ${e.message}", e)
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
