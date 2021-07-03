package com.car_dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
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
import com.google.android.gms.maps.model.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: MapView
    private lateinit var gMap: GoogleMap
    val MAPVIEW_BUNDLE_KEY :String = "MapViewBundleKey"
    var pos: Location? = null
    var mapThread : Thread? = null
    var focusMyLocation = true
    var pressNext = false
    var pressPrev = false
    var prevLoc :Location? = null
    var stillCount :Int = 0
    var firstMove = true
    lateinit var profilePic :ImageView
    lateinit var t2s :TextToSpeech
    lateinit var tempView :TextView
    lateinit var weatherView :TextView
    lateinit var humidView :TextView
    lateinit var windView :TextView

    var iconMap = mapOf("01d" to R.drawable.icon_01d, "01n" to R.drawable.icon_01n, "02d" to R.drawable.icon_02d, "02n" to R.drawable.icon_02n,
                        "03d" to R.drawable.icon_03d, "03n" to R.drawable.icon_03n, "04d" to R.drawable.icon_04d, "04n" to R.drawable.icon_04n,
                        "09d" to R.drawable.icon_09d, "09n" to R.drawable.icon_09n, "10d" to R.drawable.icon_10d, "10n" to R.drawable.icon_10n,
                        "11d" to R.drawable.icon_11d, "11n" to R.drawable.icon_11n, "13d" to R.drawable.icon_13d, "13n" to R.drawable.icon_13n,
                        "50d" to R.drawable.icon_50d, "50n" to R.drawable.icon_50n)


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()


        tempView = this.findViewById(R.id.tempView)
        weatherView = this.findViewById(R.id.weatherView)
        humidView = this.findViewById(R.id.humidView)
        windView = this.findViewById(R.id.windView)

        this.findViewById<TextView>(R.id.searchText).isSelected = false

        this.findViewById<Button>(R.id.dirBtn).setOnClickListener {
            val handler = Handler()
            handler.post {
                val intent = Intent(this@MainActivity, NavigationActivity::class.java)
                t2s.stop()
                t2s.shutdown()
                startActivity(intent)
            }
        }

        this.findViewById<Button>(R.id.credit).setOnClickListener {
            val handler = Handler()
            handler.post {
                val intent = Intent(this@MainActivity, CreditActivity::class.java)
                t2s.stop()
                t2s.shutdown()
                startActivity(intent)
            }
        }

        val python = Python.getInstance()
        val pythonFile = python.getModule("user")
        val adminStatus = pythonFile.callAttr("get_admin_status", Values.myID, Values.myName)
        val usersBtn = this.findViewById<Button>(R.id.usersBtn)
        if (adminStatus.toString() == "False") {
            usersBtn.isEnabled = false
            usersBtn.alpha = 0.5F
        } else {
            usersBtn.visibility = View.VISIBLE
            usersBtn.setOnClickListener {
                val handler: Handler = Handler()
                handler.post {
                    val intent = Intent(this@MainActivity, UsersActivity::class.java)
                    t2s.stop()
                    t2s.shutdown()
                    startActivity(intent)
                }
            }
        }
        val dateFormatter = SimpleDateFormat("dd MMMM yyyy")
        val timeFormatter = SimpleDateFormat("hh:mm:ss a")
        val nameView :TextView = this.findViewById(R.id.nameView)
        val timeView :TextView = this.findViewById(R.id.timeView)
        val dateView :TextView = this.findViewById(R.id.dateView)
        nameView.text = "\uD83D\uDC4BWelcome, ${Values.myName}"

        profilePic = this.findViewById(R.id.profileView)
        profilePic.setOnClickListener{
            val handler = Handler()
            handler.post {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                startActivity(intent)
            }
        }

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
            val inputManager: InputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            inputManager.hideSoftInputFromWindow(
                if (null == currentFocus) null
                else currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
            getSearchResults()
        }

        var recyclerView : RecyclerView = this.findViewById(R.id.musicView)

        this.findViewById<ImageView>(R.id.albumMainView).setOnLongClickListener{
            it.visibility = View.INVISIBLE
            recyclerView.visibility = View.VISIBLE
            true
        }
        val albumMain :ImageView = this.findViewById(R.id.albumMainView)
        this.findViewById<TextView>(R.id.musicInfo).setOnClickListener {
            albumMain.visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
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
        val seekBar :SeekBar = this.findViewById(R.id.seekBar)
        seekBar.max = 100
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
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
        val musicEnd :TextView = this.findViewById(R.id.musicTrackEnd)
        val musicInfo :TextView = this.findViewById(R.id.musicInfo)
        val playBtn :ImageView = this.findViewById(R.id.playBtn)
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
                                if (seekBar.progress == seekBar.max) {
                                    seekBar.progress = 0
                                    Values.Music.isPlaying = false
                                    Values.Music.durationPlayed = 0
                                    musicTime.text = "00:00"
                                    musicEnd.text = "00:00"
                                    albumMain.visibility = View.INVISIBLE
                                    recyclerView.visibility = View.VISIBLE
                                    musicInfo.text = ""
                                    Values.Music.musicPlayer!!.stop()
                                    Values.Music.musicPlayer!!.reset()
                                    Values.Music.musicPlayer = null
                                    seekBar.progress = 0
                                    playBtn.setImageResource(R.drawable.play)
                                }
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
            this.findViewById<TextView>(R.id.searchText).text = ""
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
                    mapThread?.interrupt()
                    startActivity(intent)
                    finish()
                }
            }
            builder.setNegativeButton(android.R.string.cancel,
                DialogInterface.OnClickListener { dialog, which -> })
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    override fun onEnterAnimationComplete() {

        val inputManager: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        inputManager.hideSoftInputFromWindow(
            if (null == currentFocus) null
            else currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )

        super.onEnterAnimationComplete()
        t2s.speak("Welcome to the D A A Dashboard", TextToSpeech.QUEUE_FLUSH, null)
    }

    fun getFavorites() {
        val progress = this.findViewById<ProgressBar>(R.id.progressBar)
        progress.visibility = View.VISIBLE
        val error = this.findViewById<TextView>(R.id.errorView)
        val recyclerView : RecyclerView = this.findViewById(R.id.musicView)
        recyclerView.visibility = View.INVISIBLE
        this.findViewById<ImageView>(R.id.albumMainView).visibility = View.INVISIBLE
        object : Thread() {
            override fun run() {
                val python = Python.getInstance()
                val pythonFile = python.getModule("music")
                val songsTogether =
                    pythonFile.callAttr("get_favorite_songs", Values.myID, Values.myName)
                        .toString()
                runOnUiThread {
                    if (songsTogether.isBlank()) {
                        error.text = "No Favorite Songs"
                        error.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        error.visibility = View.INVISIBLE
                        val songs = songsTogether.split(" / ")
                        var adapter = MusicAdapter(this@MainActivity, songs)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                        recyclerView.visibility = View.VISIBLE
                    }
                    progress.visibility = View.INVISIBLE
                }
            }
        }.start()
    }

    fun getSearchResults() {
        val searchText :EditText = this.findViewById(R.id.searchText)
        if (searchText.text.toString().isBlank()) {
            return
        }
        val progress = this.findViewById<ProgressBar>(R.id.progressBar)
        progress.visibility = View.VISIBLE
        this.findViewById<ImageView>(R.id.albumMainView).visibility = View.INVISIBLE
        val error = this.findViewById<TextView>(R.id.errorView)
        error.visibility = View.INVISIBLE
        val recyclerView : RecyclerView = this.findViewById(R.id.musicView)
        recyclerView.visibility = View.INVISIBLE
        object : Thread() {
            override fun run() {
                val python = Python.getInstance()
                val pythonFile = python.getModule("ytbe")
                val res = pythonFile.callAttr("get_songs", searchText.text.toString()).toString()
                runOnUiThread {
                    progress.visibility = View.INVISIBLE
                    if (res.isBlank()) {
                        error.text = "No Search Results"
                        error.visibility = View.VISIBLE
                        recyclerView.visibility = View.INVISIBLE
                    } else {
                        error.visibility = View.INVISIBLE
                        val songs = res.split(" / ")
                        val adapter = MusicAdapter(this@MainActivity, songs)
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                        recyclerView.visibility = View.VISIBLE
                    }
                    progress.visibility = View.INVISIBLE
                }
            }
        }.start()
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
        iconMap[weather[5]]?.let { this.findViewById<ImageView>(R.id.iconView).setImageResource(it) }
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
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        object : Thread() {
            override fun run() {
                val python = Python.getInstance()
                val pythonFile = python.getModule("main")
                val imageURL = pythonFile.callAttr("get_profile_pic", Values.myID, Values.myName).toString()
                DownloadImageTask(profilePic).execute(imageURL)
            }
        }.start()
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

        gMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_styles))

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
                                setUserLocationMarker(gMap.myLocation)
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
                                    val newValue = String.format("%.2f", speedView.text.split(" ")[0].toFloat() - 5)
                                    if (newValue.contains("-")) {
                                        speedView.text = "0.00"
                                    } else {
                                        speedView.text = newValue
                                    }
                                } else {
                                    speedView.text = String.format("%.2f", value)
                                }
                            }
                            prevLoc = gMap.myLocation
                        }
                    }
                } catch (e: InterruptedException) {
                }
            }
        }
        (mapThread as Thread).start()
    }

    override fun onBackPressed() {

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
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))
        } else {
            //use the previously created marker
            userLocationMarker!!.setPosition(latLng)
            userLocationMarker!!.setRotation(location.bearing)
            gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f))
        }
    }
}