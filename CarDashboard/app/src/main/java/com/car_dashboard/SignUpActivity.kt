package com.car_dashboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        this.findViewById<TextView>(R.id.personName).text = Values.myName

        this.findViewById<Button>(R.id.button5).setOnClickListener {
            checkValid()
        }
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
            handler.post{
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {

    }
}