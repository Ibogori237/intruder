package com.example.intruder

import android.os.Bundle
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import android.util.Log

class MainActivity : FlutterFragmentActivity() {

    private val CHANNEL = "com.example.intruder/channel"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "takePhotoFromNative" -> {
                    try {
                        PhotoCapture.takePhoto(this) { photoPath, locationLink ->
                            if (photoPath.isNotEmpty() && locationLink.isNotEmpty()) {
                                Log.d("MainActivity", "Photo saved at: $photoPath, Location: $locationLink")
                                result.success("Photo saved at: $photoPath, Location: $locationLink")
                            } else {
                                result.error("ERROR", "Photo capture or location retrieval failed", null)
                            }
                        }
                    } catch (e: Exception) {
                        result.error("ERROR", "Photo capture failed: ${e.message}", null)
                    }
                }
                "getLocation" -> {
                    try {
                        LocationHelper.getLocation(this) { locationLink ->
                            Log.d("MainActivity", "Location obtained: $locationLink")
                            result.success(locationLink)
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error getting location: ${e.message}", e)
                        result.error("ERROR", "Error getting location: ${e.message}", null)
                    }
                }
                "activateDeviceAdmin" -> {
                    IntruderService.requestDeviceAdmin(this)
                    result.success("Device admin activated")
                }
                else -> result.notImplemented()
            }
        }
    }
}