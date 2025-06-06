package com.example.intruder

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

object EmailSender {
    fun sendEmail(context: Context, location: String) {
        val dir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM), "Intruder")
        val latestPhoto = dir.listFiles()?.maxByOrNull { it.lastModified() } ?: return

        val uri = Uri.fromFile(latestPhoto)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("ton.email@exemple.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Intrusion détectée")
            putExtra(Intent.EXTRA_TEXT, "Voici la localisation de l'intrusion : $location")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
