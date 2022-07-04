package com.example.musicplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.musicplayer.databinding.ActivityFavoriteBinding

class favoriteActivity : AppCompatActivity() {
    private lateinit var  binding: ActivityFavoriteBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPink)
        binding=ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.backBtnFav.setOnClickListener { finish() }
    }
}