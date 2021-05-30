package com.car_dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import org.json.JSONObject


class UserAdapter(ct : Context, songs : List<String>) : RecyclerView.Adapter<UserAdapter.UserHolder>() {

    var userList : ArrayList<JSONObject> = ArrayList()
    var context : Context = ct

    init {
        for (s in songs) {
            userList.add(JSONObject(s))
        }
    }

    class UserHolder(itemView: View, ct: Context) : RecyclerView.ViewHolder(itemView) {

        var userName :TextView = itemView.findViewById(R.id.userName);
        var userStatus :TextView = itemView.findViewById(R.id.userStatus);
        var userPIN :TextView = itemView.findViewById(R.id.userPinInfo);
        var removeBtn :ImageView = itemView.findViewById(R.id.removeBtn);
        var card :ConstraintLayout = itemView.findViewById(R.id.outerCard)

        fun removeUser(): Boolean {
            val python = Python.getInstance()
            val pythonFile = python.getModule("user")
            return pythonFile.callAttr("remove_user", Values.myID, userName.text).toBoolean()
        }

        fun toggleAdmin(): Boolean {
            val python = Python.getInstance()
            val pythonFile = python.getModule("user")
            return pythonFile.callAttr("toggle_admin_status", Values.myID, userName.text).toBoolean()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view :View = inflater.inflate(R.layout.my_users, parent, false)
        return UserHolder(view, context)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {
        holder.userName.text = userList[position]["Name"] as String
        holder.userPIN.text = "PIN: " + userList[position]["PIN"] as String
        holder.userStatus.text = "Admin Privileges: " + userList[position]["is_admin"] as String
        if (!holder.userName.text.equals(Values.myName)) {
            holder.removeBtn.setOnClickListener {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setCancelable(true)
                builder.setTitle("Remove User Dialog")
                builder.setMessage("Are you sure you want to remove User " + holder.userName.text)
                builder.setPositiveButton("Confirm"
                ) { dialog, which ->
                    if (holder.removeUser()) {
                        userList.removeAt(position)
                        notifyDataSetChanged()
                    }
                }
                builder.setNegativeButton(android.R.string.cancel,
                    DialogInterface.OnClickListener { dialog, which -> })
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
            holder.card.setOnLongClickListener {
                if (holder.toggleAdmin()) {
                    val python = Python.getInstance()
                    val pythonFile = python.getModule("user")
                    val adminStatus = pythonFile.callAttr("get_admin_status", Values.myID, holder.userName.text)
                    holder.userStatus.text = "Admin Privileges: " + adminStatus.toString()
                }
                true
            }
        } else {
            holder.removeBtn.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

}