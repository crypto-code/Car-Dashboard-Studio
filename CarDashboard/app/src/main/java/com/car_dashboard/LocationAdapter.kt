package com.car_dashboard

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject


class LocationAdapter(ct : Context, locs : ArrayList<JSONObject>, map: GoogleMap) : RecyclerView.Adapter<LocationAdapter.LocationHolder>() {

    var locList : ArrayList<JSONObject> = locs
    var context : Context = ct
    var gMap :GoogleMap = map

    class LocationHolder(itemView: View, ct: Context, map: GoogleMap) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var locName :TextView = itemView.findViewById(R.id.locName);
        var locCoord :TextView = itemView.findViewById(R.id.locCoord);
        var locPlace :TextView = itemView.findViewById(R.id.locPlace);
        var locDist :TextView = itemView.findViewById(R.id.locDist);
        var favBtn :ImageView = itemView.findViewById(R.id.favBtn)
        var mapIcon :ImageView = itemView.findViewById(R.id.locIcon);
        var card :ConstraintLayout = itemView.findViewById(R.id.card)
        lateinit var latLng :LatLng;
        var context :Context = ct
        var gMap :GoogleMap = map
        lateinit var locID :String;
        lateinit var locInfo :JSONObject;
        var isFav :Boolean = false;

        private fun toggleFav() {
            val python = Python.getInstance()
            val pythonFile = python.getModule("nav")
            if (isFav) {
                val removeLoc = pythonFile.callAttr("remove_favorites", Values.myID, Values.myName, locID).toBoolean()
                if(removeLoc) {
                    favBtn.setImageResource(R.drawable.heart)
                    isFav = false
                }
            } else {
                val addLoc = pythonFile.callAttr("add_to_favorites", Values.myID, Values.myName, locID, locInfo.toString()).toBoolean()
                if(addLoc) {
                    favBtn.setImageResource(R.drawable.red_heart)
                    isFav = true
                }
            }
        }

        private fun focus() {
            gMap.clear()
            gMap.addMarker(MarkerOptions().position(latLng).title(locName.text.toString()))
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            Values.myDest = latLng
            (context as Activity).findViewById<Button>(R.id.navigateBtn).isEnabled = true
        }

        init {
            locName.isSelected = true
            locPlace.isSelected = true
            favBtn.setOnClickListener{
                toggleFav()
            }
            locName.setOnClickListener {
                focus()
            }
            mapIcon.setOnClickListener{
                focus()
            }
        }

        override fun onClick(p0: View?) {
            focus()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationHolder {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view :View = inflater.inflate(R.layout.my_loc, parent, false)
        return LocationHolder(view, context, gMap)
    }

    override fun onBindViewHolder(holder: LocationHolder, position: Int) {
        holder.locInfo = locList[position]
        holder.locName.text = locList[position]["name"] as String
        holder.locPlace.text = locList[position]["place"].toString()
        holder.locDist.text = locList[position]["distance"].toString()
        holder.locID = locList[position]["id"].toString()
        holder.locCoord.text = locList[position]["lat"].toString() + ", " + locList[position]["lon"].toString()
        holder.latLng = LatLng(locList[position]["lat"].toString().toDouble(), locList[position]["lon"].toString().toDouble())
        val python = Python.getInstance()
        val pythonFile = python.getModule("nav")
        holder.isFav = pythonFile.callAttr("is_favorite", Values.myID, Values.myName, holder.locID).toBoolean()
        if (holder.isFav) {
            holder.favBtn.setImageResource(R.drawable.red_heart)
        } else {
            holder.favBtn.setImageResource(R.drawable.heart)
        }
    }

    override fun getItemCount(): Int {
        return locList.size
    }

}