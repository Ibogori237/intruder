package com.example.intruder

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class IntruderService : DeviceAdminReceiver() {

    override fun onPasswordFailed(context: Context, intent: Intent) {
        Toast.makeText(context, "Échec de tentative de déverrouillage détecté", Toast.LENGTH_SHORT).show()
        PhotoCapture.takePhoto(context)
        LocationHelper.getLocation(context) { location ->
            EmailSender.sendEmail(context, location)
        }
    }

    companion object {
        fun requestDeviceAdmin(activity: android.app.Activity) {
            val componentName = ComponentName(activity, IntruderService::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName) // Fixed type
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Autoriser Intruder à surveiller les tentatives.")
            }
            activity.startActivityForResult(intent, 1)
        }
    }
}