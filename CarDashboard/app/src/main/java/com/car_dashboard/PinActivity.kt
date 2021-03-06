package com.car_dashboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.chaquo.python.Python
import java.util.*

class PinActivity : AppCompatActivity() {

    lateinit var t2s : TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        val handler = Handler()

        findViewById<Button>(R.id.button21).setOnClickListener{
            handler.post {
                val intent = Intent(this@PinActivity, LoginActivity::class.java)
                t2s.stop()
                t2s.shutdown()
                startActivity(intent)
            }
        }

        findViewById<Button>(R.id.button20).setOnClickListener{
            checkPin()
        }

        t2s = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                t2s.language = Locale.UK;
            }
        })
        t2s.setSpeechRate(0.8F)
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        findViewById<EditText>(R.id.inputPIN).setText("")
        t2s.speak("Please enter your pin and click to start the engine", TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun checkPin() {
        val handler = Handler()
        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val checkPin = pythonFile.callAttr("check_pin", Values.myID,  findViewById<EditText>(R.id.inputPIN).text.toString())

        if (checkPin.toString().isBlank()) {
           Toast.makeText(this, "Invalid PIN", Toast.LENGTH_LONG).show()
        } else {
            Values.myName = checkPin.toString()
            handler.post {
                val intent = Intent(this@PinActivity, MainActivity::class.java)
                t2s.stop()
                t2s.shutdown()
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

    override fun onBackPressed() {

    }
}