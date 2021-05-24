package com.car_dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.chaquo.python.Python

class PinActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        val handler = Handler()

        findViewById<Button>(R.id.button21).setOnClickListener{
            handler.post {
                val intent = Intent(this@PinActivity, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                startActivity(intent)
            }
        }

        findViewById<Button>(R.id.button20).setOnClickListener{
            checkPin()
        }
    }

    fun checkPin() {
        val handler = Handler()
        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val checkPin = pythonFile.callAttr("check_pin", Values.myID,  findViewById<EditText>(R.id.inputPIN).text.toString())

        if (checkPin.toString().isBlank()) {
           Toast.makeText(this, "Invalid PIN", Toast.LENGTH_LONG).show()
        } else {
            handler.post {
                val intent = Intent(this@PinActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                startActivity(intent)
            }
        }
    }
}