package com.car_dashboard

import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class MusicPlayer() {

    var isPlaying :Boolean = false
    var musicPlayer : MediaPlayer? = null
    var durationPlayed :Int = 0

}