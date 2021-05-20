package com.example.cardashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.chaquo.python.Python
import kotlinx.android.synthetic.main.activity_start.*

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val python = Python.getInstance()
        val pythonFile = python.getModule("face")
        val helloWorldString = pythonFile.callAttr("helloworld")
        textView2.text = helloWorldString.toString()
    }

    fun loginWindow(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent)
    }

    fun registerWindow(view: View) {

    }

    override fun onBackPressed() {

    }
}