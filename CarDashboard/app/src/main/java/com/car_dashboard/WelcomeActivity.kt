package com.car_dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.Window
import android.widget.Toast
import com.chaquo.python.Python

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
    }

    override fun onBackPressed() {

    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        val delayMillis = 3000L

        val androidID: String? = Settings.Secure.getString(baseContext.contentResolver, Settings.Secure.ANDROID_ID)
        Values.myID = androidID

        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val checkDevice = pythonFile.callAttr("check_device", androidID)
        val handler = Handler()

        if (checkDevice.toBoolean()) {
            Values.isFirst = false
            handler.postDelayed(Runnable {
                val intent = Intent(this, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent)
            }, delayMillis)
        } else {
            Values.isFirst = true
            handler.postDelayed(Runnable {
                val intent = Intent(this, SignUpActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent)
            }, delayMillis)
        }
    }
}