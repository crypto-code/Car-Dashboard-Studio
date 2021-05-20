package com.example.cardashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python


class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        val delayMillis = 3000L

        var androidID: String? = Settings.Secure.ANDROID_ID

        val python = Python.getInstance()
        val pythonFile = python.getModule("face")
        val checkDevice = pythonFile.callAttr("check_device", androidID)
        val handler = Handler()

        if (checkDevice.toBoolean()) {
            handler.postDelayed(Runnable {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent)
            }, delayMillis)
        } else {
            handler.postDelayed(Runnable {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent)
            }, delayMillis)
        }

    }

    override fun onBackPressed() {

    }

}