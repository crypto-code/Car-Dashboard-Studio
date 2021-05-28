package com.car_dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashSet


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: MapView
    private lateinit var gMap: GoogleMap
    val MAPVIEW_BUNDLE_KEY :String = "MapViewBundleKey"
    var pos: Location? = null
    lateinit var mapThread :Thread
    var focusMyLocation = true
    var pressNext = false
    var pressPrev = false
//    var playlist: MutableSet<String> = HashSet()
    var prevLoc :Location? = null
    var stillCount :Int = 0
    var firstMove = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Temporary
//        Values.myName = "ryk"
//        Values.myID = "8d15b759c609fc72"

        val dateFormatter = SimpleDateFormat("dd MMMM yyyy")
        val timeFormatter = SimpleDateFormat("hh:mm:ss a")
        val nameView :TextView = this.findViewById(R.id.nameView)
        val timeView :TextView = this.findViewById(R.id.timeView)
        val dateView :TextView = this.findViewById(R.id.dateView)
        nameView.text = "Welcome, ${Values.myName}"

        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(500)
                        runOnUiThread {
                            val today = Date()
                            timeView.text = timeFormatter.format(today).toString()
                            dateView.text = dateFormatter.format(today).toString()
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        thread.start()

        GoogleMapInit(savedInstanceState)

        this.findViewById<ImageView>(R.id.searchBtn).setOnClickListener{
            getSearchResults()
        }


        this.findViewById<ImageView>(R.id.playBtn).setOnClickListener{
            if (Values.Music.isPlaying) {
                Values.Music.durationPlayed = Values.Music.musicPlayer!!.currentPosition
                Values.Music.musicPlayer!!.stop()
                this.findViewById<ImageView>(R.id.playBtn).setImageResource(R.drawable.play)
                Values.Music.isPlaying = false
            } else if(Values.Music.musicPlayer != null) {
                Values.Music.musicPlayer!!.prepareAsync()
                this.findViewById<ImageView>(R.id.playBtn).setImageResource(R.drawable.pause)
                Values.Music.isPlaying = true
            }
        }
        var seekBar :SeekBar = this.findViewById(R.id.seekBar)
        seekBar.max = 100
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                // TODO Auto-generated method stub
                if (fromUser and Values.Music.isPlaying) {
                    Values.Music.musicPlayer!!.seekTo(progress * Values.Music.musicPlayer!!.duration / 100)
                } else if (fromUser and !Values.Music.isPlaying) {
                    Values.Music.durationPlayed = progress * Values.Music.musicPlayer!!.duration / 100
                }
            }
        })

        seekBar.setOnTouchListener{p0, p1 ->
            !Values.Music.isPlaying
        }

        val musicTime :TextView = this.findViewById(R.id.musicTrackStart)
        var seekBarHandler :Thread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        if (Values.Music.isPlaying) {
                            runOnUiThread {
                                seekBar.progress =
                                    (Values.Music.musicPlayer!!.currentPosition * 100 / Values.Music.musicPlayer!!.duration)
                                val millis = Values.Music.musicPlayer!!.currentPosition.toLong()
                                musicTime.text = String.format("%02d:%02d",
                                    TimeUnit.MILLISECONDS.toMinutes(millis),
                                    TimeUnit.MILLISECONDS.toSeconds(millis) -
                                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                                );
                            }
                        }
                        sleep(200)
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        seekBarHandler.start()

        this.findViewById<ImageView>(R.id.nextBtn).setOnTouchListener { p0, p1 ->
            if (p1?.action == MotionEvent.ACTION_DOWN) {
                pressNext = true
            } else if (p1?.action == MotionEvent.ACTION_UP) {
                pressNext = false
            }
            true
        }

        this.findViewById<ImageView>(R.id.prevBtn).setOnTouchListener { p0, p1 ->
            if (p1?.action == MotionEvent.ACTION_DOWN) {
                pressPrev = true
            } else if (p1?.action == MotionEvent.ACTION_UP) {
                pressPrev = false
            }
            true
        }

        object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        if (Values.Music.isPlaying && pressNext && Values.Music.musicPlayer!!.currentPosition + 10000 < Values.Music.musicPlayer!!.duration) {
                            Values.Music.musicPlayer!!.seekTo(Values.Music.musicPlayer!!.currentPosition + 10000)
                        } else if (Values.Music.isPlaying && pressPrev && Values.Music.musicPlayer!!.currentPosition > 10000) {
                            Values.Music.musicPlayer!!.seekTo(Values.Music.musicPlayer!!.currentPosition - 10000)
                        }
                        sleep(1000)
                    }
                } catch (e: InterruptedException) {
                }
            }
        }.start()

        getFavorites()

        this.findViewById<Button>(R.id.favBtn).setOnClickListener {
            getFavorites()
        }
    }

    fun getFavorites() {
        val python = Python.getInstance()
        val pythonFile = python.getModule("music")
        val checkFav = pythonFile.callAttr("has_favorites", Values.myID, Values.myName).toBoolean()
        val error = this.findViewById<TextView>(R.id.errorView)
        if (!checkFav) {
            error.text = "No Favorite Songs"
            error.visibility = View.VISIBLE
            return
        }
        error.visibility = View.INVISIBLE
        val songs = pythonFile.callAttr("get_favorite_songs", Values.myID, Values.myName).toString().split(" / ")
        var adapter = MusicAdapter(this, songs)
        var recyclerView : RecyclerView = this.findViewById(R.id.musicView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun getSearchResults() {
        val python = Python.getInstance()
        val pythonFile = python.getModule("ytbe")
        val searchText :EditText = this.findViewById(R.id.searchText)
        if (searchText.text.toString().isBlank()) {
            return
        }
        val res = pythonFile.callAttr("get_songs", searchText.text.toString()).toString()
        val error = this.findViewById<TextView>(R.id.errorView)
        if (res.isBlank()) {
            error.text = "No Search Results"
            error.visibility = View.VISIBLE
            return
        }
        error.visibility = View.INVISIBLE
        val songs = res.split(" / ")
        val adapter = MusicAdapter(this, songs)
        val recyclerView : RecyclerView = this.findViewById(R.id.musicView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun getWeather(lat: Double, lon: Double) {
        val python = Python.getInstance()
        val pythonFile = python.getModule("main")
        val weather = pythonFile.callAttr("get_weather", lat, lon).toString().split(" / ")
        this.findViewById<TextView>(R.id.tempView).text = weather[1]
        this.findViewById<TextView>(R.id.zoneView).text = weather[0].split("/")[1 ]
        this.findViewById<TextView>(R.id.weatherView).text = weather[2]
        this.findViewById<TextView>(R.id.humidView).text = "Humidity: " + weather[3]
        this.findViewById<TextView>(R.id.windView).text = "Wind Speed: " + weather[4]
    }

    private fun GoogleMapInit(savedInstanceState: Bundle?) {
        var mapViewBundle :Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        mMap = findViewById(R.id.map)
        mMap.onCreate(mapViewBundle)
        mMap.getMapAsync(this)
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
        gMap  = map
        map.isMyLocationEnabled = true
        if (pos != null) {
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(pos!!.latitude, pos!!.longitude),
                15F
            ))
        }
        gMap.mapType = 1
        gMap.setOnMyLocationClickListener {
            focusMyLocation = true
        }

        gMap.setOnMapLongClickListener {
            focusMyLocation = false
        }

        gMap.setOnMyLocationButtonClickListener {
            focusMyLocation = true
            return@setOnMyLocationButtonClickListener focusMyLocation
        }

        gMap.isTrafficEnabled = true
        gMap.isBuildingsEnabled = true
        var speedView :TextView = this.findViewById(R.id.speedView)
        mapThread = object : Thread() {
            override fun run() {
                try {
                    while (!this.isInterrupted) {
                        sleep(250)
                        runOnUiThread {
                            if (gMap.myLocation != null && focusMyLocation) {
                                var cameraPosition: CameraPosition = CameraPosition.Builder()
                                    .target(
                                        LatLng(
                                            gMap.myLocation.latitude,
                                            gMap.myLocation.longitude
                                        )
                                    )
                                    .tilt(60f).zoom(20f).bearing(gMap.myLocation.bearing).build()
                                if (firstMove) {
                                    gMap.moveCamera(
                                        CameraUpdateFactory.newCameraPosition(
                                            cameraPosition
                                        )
                                    );
                                    firstMove = false
                                } else {
                                    gMap.animateCamera(
                                        CameraUpdateFactory.newCameraPosition(
                                            cameraPosition
                                        ), 200, null
                                    );
                                }
                                getWeather(gMap.myLocation.latitude, gMap.myLocation.longitude)
                            }
                            if (prevLoc != null && gMap.myLocation != null) {
                                var value =  (gMap.myLocation.speed * 18/5)
                                if (gMap.myLocation.distanceTo(prevLoc) < 0.5) {
                                    stillCount++
                                } else {
                                    stillCount = 0
                                }
                                if (stillCount >= 8) {
                                    val newValue = String.format("%.2f  Km/hr", speedView.text.split(" ")[0].toFloat() - 5)
                                    if (newValue.contains("-")) {
                                        speedView.text = "0.00  Km/hr"
                                    } else {
                                        speedView.text = newValue
                                    }
                                } else {
                                    speedView.text = String.format("%.2f  Km/hr", value)
                                }
                            }
                            prevLoc = gMap.myLocation
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        mapThread.start()
    }
}