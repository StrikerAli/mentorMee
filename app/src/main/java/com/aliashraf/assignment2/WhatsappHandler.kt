package com.aliashraf.assignment2
import android.content.Context
import android.content.Intent
import android.net.Uri

class WhatsappHandler(private val context: Context) {

    fun initiateVideoCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$phoneNumber/?action=video")
        context.startActivity(intent)
    }
    fun initiateAudioCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse("https://wa.me/$phoneNumber/?action=audio")
        context.startActivity(intent)
    }
}
