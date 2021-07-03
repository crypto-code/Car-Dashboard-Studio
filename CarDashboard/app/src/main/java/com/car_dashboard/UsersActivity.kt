package com.car_dashboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python

class UsersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        this.findViewById<Button>(R.id.button9).setOnClickListener {
            val handler = Handler()
            handler.post {
                val intent = Intent(this, AddUserActivity::class.java)
                startActivity(intent)
            }
        }
    }

     fun getUsers() {
        val python = Python.getInstance()
        val pythonFile = python.getModule("user")
        val users = pythonFile.callAttr("get_all_users", Values.myID).toString().split(" / ")

        val adapter = UserAdapter(this, users)
        val recyclerView : RecyclerView = this.findViewById(R.id.userView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        getUsers()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
    }

}