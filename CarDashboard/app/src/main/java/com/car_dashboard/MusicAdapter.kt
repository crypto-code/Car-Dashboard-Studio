package com.car_dashboard

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit


class MusicAdapter(ct : Context, songs : List<String>) : RecyclerView.Adapter<MusicAdapter.MusicHolder>() {

    var songList : ArrayList<JSONObject> = ArrayList()
    var context : Context = ct

    init {
        for (s in songs) {
            songList.add(JSONObject(s))
        }
    }

    class MusicHolder(itemView: View, ct: Context) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        var musicName :TextView = itemView.findViewById(R.id.musicName);
        var albumArt :ImageView = itemView.findViewById(R.id.albumArt);
        var musicArtist :TextView = itemView.findViewById(R.id.musicArtist);
        var musicDuration :TextView = itemView.findViewById(R.id.musicDuration);
        var likeBtn :ImageView = itemView.findViewById(R.id.likeBtn)
        var card :ConstraintLayout = itemView.findViewById(R.id.card)
        lateinit var musicURL:String;
        var context :Context = ct
        lateinit var songID :String;
        var isFav :Boolean = false;

        private fun playThis() {
            try {
                val mediaPlayer = MediaPlayer()
                if (Values.Music.isPlaying && Values.Music.musicPlayer != null) {
                    Values.Music.musicPlayer!!.stop()
                    Values.Music.musicPlayer!!.reset()
                    Values.Music.isPlaying = false
                }
                mediaPlayer.setDataSource(musicURL)
                Values.Music.musicPlayer = mediaPlayer
                Values.Music.durationPlayed = 0
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    Toast.makeText(context, "Playing ${musicName.text}", Toast.LENGTH_LONG).show()
                    Values.Music.isPlaying = true
                    Values.Music.musicPlayer!!.seekTo(Values.Music.durationPlayed)
                    (context as Activity).findViewById<ImageView>(R.id.playBtn).setImageResource(R.drawable.pause)
                    (context as Activity).findViewById<TextView>(R.id.musicInfo).text = "Track: " + musicName.text + " Artist: " + musicArtist.text
                    (context as Activity).findViewById<TextView>(R.id.musicTrackEnd).text = musicDuration.text
                    it.start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun toggleFav() {
            val python = Python.getInstance()
            val pythonFile = python.getModule("music")
            if (isFav) {
                val removeSong = pythonFile.callAttr("remove_favorites", Values.myID, Values.myName, songID).toBoolean()
                if(removeSong) {
                    likeBtn.setImageResource(R.drawable.heart)
                    isFav = false
                }
            } else {
                val addSong = pythonFile.callAttr("add_to_favorites", Values.myID, Values.myName, songID, musicName.text.toString()).toBoolean()
                if(addSong) {
                    likeBtn.setImageResource(R.drawable.red_heart)
                    isFav = true
                }
            }
        }

        init {
            musicName.setOnClickListener{
                playThis()
            }
            albumArt.setOnClickListener{
                playThis()
            }
            musicName.isSelected = true
            musicArtist.isSelected = true
            likeBtn.setOnClickListener{
                toggleFav()
            }
            card.setOnClickListener{
                playThis()
            }
        }

        override fun onClick(p0: View?) {
            playThis()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicHolder {
        val inflater : LayoutInflater = LayoutInflater.from(context)
        val view :View = inflater.inflate(R.layout.my_row, parent, false)
        return MusicHolder(view, context)
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {
        holder.musicName.text = songList[position]["title"] as String
        holder.musicArtist.text = songList[position]["artists"] as String
        holder.musicDuration.text = songList[position]["duration"] as String
        DownloadImageTask(holder.albumArt).execute(songList[position]["thumbnail"] as String)
        holder.musicURL = songList[position]["url"] as String
        holder.songID = songList[position]["id"] as String
        val python = Python.getInstance()
        val pythonFile = python.getModule("music")
        holder.isFav = pythonFile.callAttr("is_favorite", Values.myID, Values.myName, holder.songID).toBoolean()
        if (holder.isFav) {
            holder.likeBtn.setImageResource(R.drawable.red_heart)
        } else {
            holder.likeBtn.setImageResource(R.drawable.heart)
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

}