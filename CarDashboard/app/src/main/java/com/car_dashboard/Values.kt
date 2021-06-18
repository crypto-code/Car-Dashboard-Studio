package com.car_dashboard

import android.media.AudioManager
import android.media.MediaPlayer
import android.speech.tts.TextToSpeech
import com.google.android.gms.maps.model.LatLng

object Values {

    var myID: String? = ""
    var myName: String? = ""
    var myPIN: String? = ""
    var isFirst: Boolean = true

    var Music: MusicPlayer = MusicPlayer()

    var newName: String? = ""
    var newPIN: String? = ""

    var myDest : LatLng? = null

    lateinit var t2s : TextToSpeech
}