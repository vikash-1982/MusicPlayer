package com.example.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.IBinder
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(),ServiceConnection,MediaPlayer.OnCompletionListener{
    companion object{
        lateinit var musicListPA:ArrayList<Music>
        var songPosition:Int=0
        var isPlaying:Boolean=false
        var musicService:MusicService?=null
        lateinit var binding:ActivityPlayerBinding
        var repeat:Boolean=false
        var time15:Boolean=false
        var time30:Boolean=false
        var time60:Boolean=false

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding= ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for starting service

        intializeLayout()
        binding.btnNext.setOnClickListener {
          prevNext(Increment = true)
        }
        binding.btnPrev.setOnClickListener {
           prevNext(Increment = false)
        }
        binding.playPause.setOnClickListener {
           if(isPlaying)
           {
               PauseMusic()
           }
            else
           {
               PlayMusic()
           }
        }
        binding.backBtn.setOnClickListener { finish() }
        binding.seekbarPa.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) =Unit

            override fun onStopTrackingTouch(seekBar: SeekBar?)=Unit

        })
        binding.repeatPA.setOnClickListener {
            if(!repeat)
            {
                repeat=true
                binding.repeatPA.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            }
            else
            {
                repeat=false
                binding.repeatPA.setColorFilter(ContextCompat.getColor(this, R.color.coolred))
            }
        }
        binding.btnEqualizerPA.setOnClickListener {
            try{
                val intent=Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
                intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE,AudioEffect.CONTENT_TYPE_MUSIC)
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,baseContext.packageName)
                startActivityForResult(intent,41)
            }catch(e:Exception){
                Toast.makeText(this,"Not Equilizer",Toast.LENGTH_SHORT).show()
            }

        }
        binding.btnTimerPA.setOnClickListener {
          var timer= time15 || time30|| time60
            if(!timer)
            {
                showBottomsheetDailog()
            }
            else
            {
                val builder= MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop")
                    .setMessage("Do you want to close timer?")
                    .setPositiveButton("Yes")
                    { _,_->
                        time15=false
                        time30=false
                        time60=false
                        binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this,R.color.coolred))
                    }
                    .setNegativeButton("No"){
                            dialog,_ ->
                        dialog.dismiss()
                    }
                val custom=builder.create()
                custom.show()
                custom.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
                custom.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
            }
        }
    }

    private fun setLayout()
    {
        Glide.with(this).load(musicListPA[songPosition].artUri).apply(RequestOptions().placeholder(R.drawable.musicplayericon).centerCrop())
            .into(binding.SongImage)
        binding.SongName.text= musicListPA[songPosition].title
        if(repeat)
        {
            binding.repeatPA.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
        }
        if(time15|| time30|| time60)
        {
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
        }
    }
    private fun media()
    {
        try {
            if (musicService!!.mediaPlayer== null)
               musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying=true
            binding.playPause.setIconResource(R.drawable.ic_baseline_pause_24)
            musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
            binding.start.text= formatDuration(musicService!!.mediaPlayer!!.currentPosition!!.toLong())
            binding.end.text= formatDuration(musicService!!.mediaPlayer!!.duration!!.toLong())
            binding.seekbarPa.progress=0
            binding.seekbarPa.max= musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
        }catch (e:Exception){return}
    }
    private fun PlayMusic()
    {
        binding.playPause.setIconResource(R.drawable.ic_baseline_pause_24)
        musicService!!.showNotification(R.drawable.ic_baseline_pause_24)
        musicService!!.mediaPlayer!!.start()
    }
    private fun PauseMusic()
    {
        binding.playPause.setIconResource(R.drawable.ic_baseline_play_arrow_24)
        musicService!!.showNotification(R.drawable.ic_baseline_play_arrow_24)
        isPlaying=false
        musicService!!.mediaPlayer!!.pause()
    }
    private fun intializeLayout()
    {
        songPosition=intent.getIntExtra("index",0)
        when(intent.getStringExtra("class"))
        {
            "NowPlaying"->{
                setLayout()

            }
            "MusicAdapterSearch"->{
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter"->
            {
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "MainActivity" ->{
                val intent= Intent(this,MusicService::class.java)
                bindService(intent,this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA= ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()

            }
        }
    }
    private fun prevNext(Increment:Boolean)
    {
        if(Increment)
        {
            setSongPosition(increment=true)

            setLayout()
            media()
        }
        else
        {
            setSongPosition(increment = false)
            setLayout()
            media()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder=service as MusicService.MyBinder
        musicService=binder.currentService()
        media()
        musicService!!.seekBarSetup()

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService=null
    }

    override fun onCompletion(mp: MediaPlayer?) {
       setSongPosition(increment = true)
        media()
        try{
            setLayout()
        }catch(e:Exception){return}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==41 || resultCode== RESULT_OK)
        {
            return
        }
    }
    private fun showBottomsheetDailog()
    {
        val dialog=BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.time_15)?.setOnClickListener {
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this,R.color.purple_500))
            time15=true
            Thread{Thread.sleep(15*60000)
            if(time15)
           exitApplication()}.start()
           dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.time_30)?.setOnClickListener {
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            time30=true
            Thread{Thread.sleep(30*60000)
                if(time30)
                    exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.time_60)?.setOnClickListener {
            binding.btnTimerPA.setColorFilter(ContextCompat.getColor(this, R.color.teal_200))
            time60=true
            Thread{Thread.sleep(60*60000)
                if(time60)
                    exitApplication()}.start()
            dialog.dismiss()
        }

    }

}