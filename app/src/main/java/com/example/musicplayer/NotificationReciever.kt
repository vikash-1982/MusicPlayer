package com.example.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReciever:BroadcastReceiver (){
    override fun onReceive(context: Context?, intent: Intent?) {
      when(intent?.action){
          ApplicationClass.PREVIOUS ->
          {
              nextPrev(increment = false,context!!)
          }
          ApplicationClass.PLAY ->
          {
             if(PlayerActivity.isPlaying)
                 pauseMusic()
              else
                  playMusic()
          }
          ApplicationClass.NEXT ->
          {
              nextPrev(increment = true,context!!)
          }
          ApplicationClass.EXIT ->
          {
              exitApplication()
          }
        }
    }
    private fun playMusic()
    {
        PlayerActivity.isPlaying=true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        PlayerActivity.binding.playPause.setIconResource(R.drawable.ic_baseline_pause_24)
        NowPlaying.binding.songPlayFM.setIconResource(R.drawable.ic_baseline_pause_24)

    }
    private fun pauseMusic()
    {
        PlayerActivity.isPlaying=false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24)
        PlayerActivity.binding.playPause.setIconResource(R.drawable.ic_baseline_play_arrow_24)
        NowPlaying.binding.songPlayFM.setIconResource(R.drawable.ic_baseline_play_arrow_24)
    }
    private fun nextPrev(increment:Boolean,context:Context){
        setSongPosition(increment=increment)
         PlayerActivity.musicService!!.media()
        PlayerActivity.binding.playPause.setIconResource(R.drawable.ic_baseline_pause_24)
        Glide.with( context).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri).apply(
            RequestOptions().placeholder(R.drawable.musicplayericon).centerCrop())
            .into(PlayerActivity.binding.SongImage)
        PlayerActivity.binding.SongName.text= PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        Glide.with(context).load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri).apply(
            RequestOptions().placeholder(R.drawable.musicplayericon).centerCrop())
            .into(NowPlaying.binding.songImageFM)
        NowPlaying.binding.songNameFM.text= PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
    }
}