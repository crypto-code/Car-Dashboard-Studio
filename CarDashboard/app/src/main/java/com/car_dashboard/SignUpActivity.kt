package com.car_dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.*

class SignUpActivity : AppCompatActivity() {

    lateinit var t2s :TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        this.findViewById<TextView>(R.id.personName).text = Values.myName

        this.findViewById<Button>(R.id.button5).setOnClickListener {
            checkValid()
        }

        t2s = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                t2s.language = Locale.UK;
            }
        })
        t2s.setSpeechRate(0.8F)
        t2s.speak("Welcome please enter your Name and a Pin of your choice", TextToSpeech.QUEUE_FLUSH, null)
    }

    fun checkValid() {
        val personName :TextView = this.findViewById(R.id.personName)
        val pin :TextView = this.findViewById(R.id.pin)
        if (personName.text.toString().isBlank() || pin.text.toString().isBlank()) {
            Toast.makeText(this,"Name and PIN are Required", Toast.LENGTH_SHORT).show()
        } else if (personName.text.length < 3) {
            Toast.makeText(this,"Name must be minimum 3 characters long", Toast.LENGTH_LONG).show()
        } else if (pin.text.length < 4) {
            Toast.makeText(this,"Pin must be minimum 4 numbers long", Toast.LENGTH_LONG).show()
        } else {
            Values.myName = personName.text.toString()
            Values.myPIN = pin.text.toString()
            val handler = Handler()
            t2s.stop()
            t2s.shutdown()
            handler.post{
                val intent = Intent(this, RegisterActivity::class.java)

                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {

    }
}