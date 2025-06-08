package com.example.intruder

import android.content.Context
import android.hardware.camera2.*
import android.media.ImageReader
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PhotoCapture {
    private const val TAG = "PhotoCapture"

    fun takePhoto(context: Context, onPhotoSaved: (String, String) -> Unit) { // Add location callback
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            Log.d(TAG, "Attempting to open the front camera...")
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT
            }

            if (cameraId == null) {
                Log.e(TAG, "No front camera found.")
                Toast.makeText(context, "Erreur : Aucun appareil photo frontal trouvé", Toast.LENGTH_SHORT).show()
                return
            }

            val characteristics = cameraManager.getCameraCharacteristics(cameraId)
            val streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            val imageSize = streamConfigurationMap?.getOutputSizes(ImageReader::class.java)?.firstOrNull()

            if (imageSize == null) {
                Log.e(TAG, "No supported image sizes found.")
                Toast.makeText(context, "Erreur : Impossible de trouver une taille d'image prise en charge", Toast.LENGTH_SHORT).show()
                return
            }

            val imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, android.graphics.ImageFormat.JPEG, 1)
            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                val buffer = image.planes[0].buffer
                val data = ByteArray(buffer.remaining())
                buffer.get(data)
                image.close()

                LocationHelper.getLocation(context) { locationLink ->
                    val photoPath = savePhoto(context, data)
                    onPhotoSaved(photoPath, locationLink) // Pass photo path and location link
                }
            }, null)

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    Log.d(TAG, "Camera opened.")
                    val captureRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                    captureRequestBuilder.addTarget(imageReader.surface)

                    camera.createCaptureSession(listOf(imageReader.surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            Log.d(TAG, "Capture session configured.")
                            session.capture(captureRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
                                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                                    Log.d(TAG, "Photo captured.")
                                    camera.close()
                                }
                            }, null)
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e(TAG, "Failed to configure capture session.")
                            Toast.makeText(context, "Erreur : Échec de la configuration de la session de capture", Toast.LENGTH_SHORT).show()
                        }
                    }, null)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    Log.e(TAG, "Camera disconnected.")
                    Toast.makeText(context, "Erreur : Appareil photo déconnecté", Toast.LENGTH_SHORT).show()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e(TAG, "Error opening camera: $error")
                    Toast.makeText(context, "Erreur : Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show()
                }
            }, null)
        } catch (e: Exception) {
            Log.e(TAG, "Error accessing camera: ${e.message}", e)
            Toast.makeText(context, "Erreur : Impossible d'accéder à la caméra", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhoto(context: Context, data: ByteArray): String {
        return try {
            val dir = context.getExternalFilesDir("Intruder")
            if (dir != null && !dir.exists()) {
                val created = dir.mkdirs()
                Log.d(TAG, "Directory created: $created")
            }
            val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(dir, "intruder_$time.jpg")
            FileOutputStream(file).use { it.write(data) }
            Log.d(TAG, "Photo saved to: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Error saving photo: ${e.message}", e)
            Toast.makeText(context, "Erreur : Impossible de sauvegarder la photo", Toast.LENGTH_SHORT).show()
            ""
        }
    }
}