package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter
    companion object{
        lateinit var MusicListMA:ArrayList<Music>
        lateinit var musicListSearch:ArrayList<Music>
        var search:Boolean=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.coolPinkNav)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //for nav drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(requestPermission())
        intializeLayout()

        binding.btnShuffle.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index",0)
            intent.putExtra("class","MainActivity")
            startActivity(intent)
        }
        binding.btnFavorite.setOnClickListener {
            val intent = Intent(this@MainActivity, favoriteActivity::class.java)
            startActivity(intent)
        }
        binding.btnPlaylist.setOnClickListener {
            val intent = Intent(this@MainActivity, PlaylistActivity::class.java)
            startActivity(intent)

        }
        binding.navview.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item_navfeed -> {
                    Toast.makeText(this, "FeedBack Worked", Toast.LENGTH_LONG).show()
                }
                R.id.item_navsetting -> {
                    Toast.makeText(this, "Setting Worked", Toast.LENGTH_LONG).show()
                }
                R.id.item_navabout -> {
                    Toast.makeText(this, "About Worked", Toast.LENGTH_LONG).show()
                }
                R.id.item_navexit -> {
                    val builder= MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")
                        .setPositiveButton("Yes")
                        { _,_->
                            exitApplication()
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
            true
        }
    }


    private fun requestPermission() :Boolean{
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                intializeLayout()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
            }
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun intializeLayout() {
        search=false
       MusicListMA=getAllAudio()
        binding.musicRv.setHasFixedSize(true)
        //ye isiilye use kiye hain kyoki mere list main jitna song hoga utna hi ye spache banayega isse memory bchega
        binding.musicRv.setItemViewCacheSize(13)
        binding.musicRv.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this, MusicListMA)
        binding.musicRv.adapter = musicAdapter
        binding.totalSong.text = "Total Song :" + musicAdapter.itemCount.toString()
    }

    //the function use in access in music file
    @SuppressLint("Recycle", "Range")
    private fun getAllAudio(): ArrayList<Music> {
        var templist = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"//ye cursor ko btayega kon se type ka file chaiye
        val projrction = arrayOf(          //cursor ko btayenge kya kya data chaiye file se
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projrction, selection, null,
            MediaStore.Audio.Media.DATE_ADDED,null)
        if (cursor!=null) {
           if(cursor.moveToFirst())
               do{
                   val titleC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                   val idC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                   val albumC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                   val artistC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                   val pathC=cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                   val durationC=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                   val albumidC=cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                   val uri= Uri.parse("content://media/external/audio/albumart")
                   val artUriC=Uri.withAppendedPath(uri,albumidC).toString()
                   val music=Music(id=idC,
                       title=titleC,album=albumC, artist=artistC, path=pathC, duration = durationC, artUri=artUriC
                   )
                   val file= File(music.path)
                   if(file.exists())
                       templist.add(music)
               }while(cursor.moveToNext())
               cursor.close()
        }
        return templist
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService!=null)
        {
            exitApplication()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu,menu)
        val searchView=menu?.findItem(R.id.search_view)?.actionView as SearchView
         searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
             override fun onQueryTextSubmit(query: String?): Boolean=true

             override fun onQueryTextChange(newText: String?): Boolean {
                 musicListSearch= ArrayList()
                if(newText!=null)
                {
                    val userInput=newText.lowercase()
                    for(song in MusicListMA)
                    {
                        if(song.title.lowercase().contains(userInput))
                        {
                            musicListSearch.add(song)
                        }
                    }
                }
                 search=true
                 musicAdapter.updatemusiclist(searchList = musicListSearch)
                 return true
             }
         })
        return super.onCreateOptionsMenu(menu)
    }
}