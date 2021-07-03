package com.car_dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class NavigationActivity : AppCompatActivity(), OnMapReadyCallback {

    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var gMap : GoogleMap
    private lateinit var mMap : MapView
    val MAPVIEW_BUNDLE_KEY :String = "MapViewBundleKey"
    lateinit var mapThread :Thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()

        this.findViewById<Button>(R.id.searchPlaceBtn).setOnClickListener {
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                if (null == currentFocus) null
                else currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            getSearch()
        }
        GoogleMapInit(savedInstanceState)

        this.findViewById<Button>(R.id.favPlaceBtn).setOnClickListener {
            this.findViewById<TextView>(R.id.mapPlaces).text = ""
            getFavorites()
        }

        this.findViewById<Button>(R.id.navigateBtn).setOnClickListener {
            startMap(Values.myDest?.latitude.toString() + "," + Values.myDest?.longitude.toString())
        }

        this.findViewById<ProgressBar>(R.id.progressBar5).visibility = View.VISIBLE
    }

    fun getFavorites() {
        val progress = this.findViewById<ProgressBar>(R.id.progressBar5)
        progress.visibility = View.VISIBLE
        val error = this.findViewById<TextView>(R.id.errorView2)
        error.visibility = View.INVISIBLE
        val recyclerView : RecyclerView = this.findViewById(R.id.locView)
        recyclerView.visibility = View.INVISIBLE
        var lat = gMap.myLocation.latitude
        var lon =  gMap.myLocation.longitude
        object : Thread() {
            override fun run() {
                val python = Python.getInstance()
                val pythonFile = python.getModule("nav")
                val favsTogether =
                    pythonFile.callAttr("get_favorite_locs", Values.myID, Values.myName, lat, lon)
                        .toString()
                runOnUiThread {
                    if (favsTogether.isBlank()) {
                        error.text = "No Saved Locations"
                        error.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        error.visibility = View.INVISIBLE
                        val results = favsTogether.split(" / ")
                        val locList : ArrayList<JSONObject> = ArrayList()
                        for (l in results) {
                            locList.add(JSONObject(l))
                        }
                        val adapter = LocationAdapter(this@NavigationActivity, locList, gMap)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this@NavigationActivity)
                        recyclerView.visibility = View.VISIBLE
                    }
                    progress.visibility = View.INVISIBLE
                }
            }
        }.start()
    }

    private fun getSearch() {
        var searchText = this.findViewById<TextView>(R.id.mapPlaces).text
        if (searchText.isBlank()) {
            Toast.makeText(this, "Please Enter a Location", Toast.LENGTH_LONG).show()
            return
        }
        val recyclerView : RecyclerView = this.findViewById(R.id.locView)
        val error = this.findViewById<TextView>(R.id.errorView2)
        error.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
        val progress :ProgressBar = this.findViewById(R.id.progressBar5)
        progress.visibility = View.VISIBLE
        var lat = gMap.myLocation.latitude
        var lon =  gMap.myLocation.longitude
        object :Thread() {
            override fun run() {
                val python = Python.getInstance()
                val pythonFile = python.getModule("nav")
                val res = pythonFile.callAttr("get_search_results", searchText, lat, lon).toString()
                val locList : ArrayList<JSONObject> = ArrayList()
                if (res.isBlank()) {
                    runOnUiThread {
                        progress.visibility = View.INVISIBLE
                        recyclerView.visibility = View.VISIBLE
                        error.text = "No Locations Found"
                        error.visibility = View.VISIBLE
                    }
                }
                val results = res.split(" / ")
                for (l in results) {
                    locList.add(JSONObject(l))
                }
                runOnUiThread {
                    val adapter = LocationAdapter(this@NavigationActivity, locList, gMap)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@NavigationActivity)
                    progress.visibility = View.INVISIBLE
                    recyclerView.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun startMap(coord :String) {
        val uri =
            "google.navigation:q=$coord&mode=d"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent)
    }

    private fun GoogleMapInit(savedInstanceState: Bundle?) {
        var mapViewBundle :Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMap = findViewById(R.id.map2)
        mMap.onCreate(mapViewBundle)
        mMap.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1);
        }
        map.isMyLocationEnabled = true
        gMap = map
        gMap.mapType = 1
        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles))
        gMap.isTrafficEnabled = true
        gMap.isBuildingsEnabled = true

        mapThread = object : Thread() {
            override fun run() {
                try {
                    sleep(1000)
                    while(!this.isInterrupted) {
                        sleep(200)
                        runOnUiThread {
                            if (gMap.myLocation != null) {
                                setUserLocationMarker(gMap.myLocation)
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        mapThread.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }
        mMap.onSaveInstanceState(mapViewBundle)
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        mMap.onResume()
    }

    override fun onStart() {
        super.onStart()
        mMap.onStart()
    }

    override fun onStop() {
        super.onStop()
        mMap.onStop()
    }

    override fun onPause() {
        mMap.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mMap.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMap.onLowMemory()
    }


    var userLocationMarker: Marker? = null

    private fun setUserLocationMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (userLocationMarker == null) {
            //Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car))
            markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
            userLocationMarker = gMap.addMarker(markerOptions)
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            getFavorites()
        } else {
            //use the previously created marker
            userLocationMarker!!.setPosition(latLng)
            userLocationMarker!!.setRotation(location.bearing)
        }
    }

    override fun onBackPressed() {
        mapThread.interrupt()
        super.onBackPressed()
    }
}