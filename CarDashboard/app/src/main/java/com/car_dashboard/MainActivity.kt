package com.car_dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
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


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: MapView
    private lateinit var gMap: GoogleMap
    val MAPVIEW_BUNDLE_KEY :String = "MapViewBundleKey"
    var pos: Location? = null
    lateinit var mapThread :Thread
    var focusMyLocation = true
    var pressNext = false
    var pressPrev = false
    var playlist: MutableSet<String> = HashSet()
    var prevLoc :Location? = null
    var stillCount :Int = 0
    var firstMove = true
    lateinit var t2s :TextToSpeech
    lateinit var tempView :TextView
    lateinit var weatherView :TextView
    lateinit var humidView :TextView
    lateinit var windView :TextView

    lateinit var weatherUpdate :String


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tempView = this.findViewById(R.id.tempView)
        weatherView = this.findViewById(R.id.weatherView)
        humidView = this.findViewById(R.id.humidView)
        windView = this.findViewById(R.id.windView)

        val python = Python.getInstance()
        val pythonFile = python.getModule("user")
        val adminStatus = pythonFile.callAttr("get_admin_status", Values.myID, Values.myName)
        if (adminStatus.toString() == "False") {
            this.findViewById<Button>(R.id.usersBtn).visibility = View.INVISIBLE
        } else {
            this.findViewById<Button>(R.id.usersBtn).visibility = View.VISIBLE
            this.findViewById<Button>(R.id.usersBtn).setOnClickListener {
                val handler: Handler = Handler()
                mapThread.interrupt()
                handler.post {
                    val intent = Intent(this@MainActivity, UsersActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                    startActivity(intent)
                }
            }
        }

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

        t2s = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it != TextToSpeech.ERROR) {
                t2s.language = Locale.UK;
            }
        })
        t2s.setSpeechRate(0.8F)

        this.findViewById<Button>(R.id.logoutBtn).setOnClickListener {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setCancelable(true)
            builder.setTitle("Exit Dashboard Dialog")
            builder.setMessage("Are you sure you want to exit dashboard")
            builder.setPositiveButton("Confirm"
            ) { dialog, which ->
                val handler = Handler()
                handler.post {
                    val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY;
                    mapThread.interrupt()
                    startActivity(intent)
                }
            }
            builder.setNegativeButton(android.R.string.cancel,
                DialogInterface.OnClickListener { dialog, which -> })
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    override fun onEnterAnimationComplete() {
        super.onEnterAnimationComplete()
        t2s.speak("Welcome to the D A A Dashboard", TextToSpeech.QUEUE_FLUSH, null)

        object : Thread() {
            override fun run() {
                while(firstMove) {
                    sleep(500)
                }
                sleep(1000)
                t2s.speak(weatherUpdate, TextToSpeech.QUEUE_ADD, null)
            }
        }//.start()
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
        tempView.text = weather[1]
        this.findViewById<TextView>(R.id.zoneView).text = weather[0].split("/")[1]
        weatherView.text = weather[2]
        humidView.text = "Humidity: " + weather[3]
        windView.text = "Wind Speed: " + weather[4]
        if (firstMove) {
            weatherUpdate = "The current weather in your area is ${weather[2]} with a temperature of ${weather[1]} degree celsius, " +
                    "humidity of ${weather[3]} percentage and wind speed of ${weather[4]} kilometer per hour."
        }
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
                                getWeather(gMap.myLocation.latitude, gMap.myLocation.longitude)
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

    override fun onBackPressed() {

    }
}