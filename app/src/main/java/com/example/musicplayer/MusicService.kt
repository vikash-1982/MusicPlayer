package com.example.musicplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

class MusicService:Service() {
    private var mybinder=MyBinder()
    var mediaPlayer:MediaPlayer?=null
    private lateinit var mediaSession:MediaSessionCompat
    private lateinit var runnable: Runnable
    override fun onBind(intent: Intent?): IBinder {
        mediaSession=MediaSessionCompat(baseContext,"My Music")
       return mybinder
    }
    inner class MyBinder: Binder()
    {
        fun currentService():MusicService
        {
            return this@MusicService
        }
    }
    fun showNotification(playPausebtn:Int)
    {
        val prevIntent=Intent(baseContext,NotificationReciever::class.java).setAction(
            ApplicationClass.PREVIOUS)
        val prevPendingIntent=PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val playIntent=Intent(baseContext,NotificationReciever::class.java).setAction(
            ApplicationClass.PLAY)
        val playPendingIntent=PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent=Intent(baseContext,NotificationReciever::class.java).setAction(
            ApplicationClass.NEXT)
        val nextPendingIntent=PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val exitIntent=Intent(baseContext,NotificationReciever::class.java).setAction(
            ApplicationClass.EXIT)
        val exitPendingIntent=PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_UPDATE_CURRENT)

        val imageArt= getImageArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
       val image= if(imageArt!=null)
        {
            BitmapFactory.decodeByteArray(imageArt,0,imageArt.size)
        }else
        {
            BitmapFactory.decodeResource(resources,R.drawable.musicplayericon)
        }

      val notification=NotificationCompat.Builder(baseContext,ApplicationClass.CHANNEL_ID)
          .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
          .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
          .setSmallIcon(R.drawable.ic_baseline_library_music_24)
          .setLargeIcon(image)
          .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
          .setPriority(NotificationCompat.VISIBILITY_PUBLIC)
          .setOnlyAlertOnce(true)
          .addAction(R.drawable.ic_baseline_arrow_back_ios_24,"previous",prevPendingIntent)
          .addAction(playPausebtn,"play",playPendingIntent)
          .addAction(R.drawable.ic_baseline_navigate_next_24,"next",nextPendingIntent)
          .addAction(R.drawable.navexit,"exit",exitPendingIntent)
          .build()
        startForeground(41,notification)
    }
     fun media()
    {
        try {
            if (PlayerActivity.musicService!!.mediaPlayer== null)
                PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPause.setIconResource(R.drawable.ic_baseline_pause_24)
            PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
            PlayerActivity.binding.start.text= formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition!!.toLong())
            PlayerActivity.binding.end.text= formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration!!.toLong())
            PlayerActivity.binding.seekbarPa.progress=0
            PlayerActivity.binding.seekbarPa.max= PlayerActivity.musicService!!.mediaPlayer!!.duration
        }catch (e:Exception){return}
    }
    fun seekBarSetup()
    {
        runnable= Runnable {
            PlayerActivity.binding.start.text= formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition!!.toLong())
            PlayerActivity.binding.seekbarPa.progress=mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
}