package com.example.intruder

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import java.io.File

object EmailSender {
    private const val TAG = "EmailSender"

    suspend fun sendEmail(locationLink: String, photoPath: String) {
        val senderEmail = "ibrahimabakarkori235@gmail.com" // Replace with your sender email
        val senderPassword = "hijwhqkisagpihiz" // Replace with your sender email password
        val recipientEmail = "ibrahimabakargori235@gmail.com"

        val properties = Properties().apply {
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
        }

        val session = Session.getInstance(properties, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication() =
                javax.mail.PasswordAuthentication(senderEmail, senderPassword)
        })

        try {
            withContext(Dispatchers.IO) {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(senderEmail))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                    subject = "Intrusion détectée"
                }

                // Create a multipart message
                val multipart = MimeMultipart()

                // Add text part
                val textPart = MimeBodyPart()
                textPart.setText("Voici la localisation de l'intrusion : $locationLink")
                multipart.addBodyPart(textPart)

                // Add photo part
                val photoFile = File(photoPath)
                if (photoFile.exists()) {
                    val photoPart = MimeBodyPart()
                    photoPart.attachFile(photoFile)
                    multipart.addBodyPart(photoPart)
                } else {
                    Log.e(TAG, "Photo file does not exist at path: $photoPath")
                }

                // Set the content of the message
                message.setContent(multipart)

                // Send the email
                Transport.send(message)
                Log.d(TAG, "Email sent successfully.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending email: ${e.message}", e)
        }
    }
}