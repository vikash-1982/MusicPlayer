package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.MusicViewBinding

class MusicAdapter(private val context: Context,private var musicList:ArrayList<Music>): RecyclerView.Adapter<MusicAdapter.MyHolder>() {


    class MyHolder(binding: MusicViewBinding):RecyclerView.ViewHolder(binding.root) {
       val title=binding.songName
        val album=binding.songAlbum
        val image=binding.imageView
        val duration=binding.songDuration
        val root=binding.root
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicAdapter.MyHolder {

        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }
    override fun onBindViewHolder(holder: MusicAdapter.MyHolder, position: Int) {
        holder.title.text=musicList[position].title
        holder.album.text=musicList[position].album
        holder.duration.text= formatDuration(musicList[position].duration)
        Glide.with(context).load(musicList[position].artUri).apply(RequestOptions().placeholder(R.drawable.musicplayericon).centerCrop())
            .into(holder.image)
        holder.root.setOnClickListener {
           when{
               MainActivity.search->{
                   sendIntent(ref = "MusicAdapterSearch", pos = position)
               }
               else->sendIntent(ref="MusicAdapter", pos = position)
           }
        }
    }

    override fun getItemCount(): Int {
       return musicList.size
    }
    fun updatemusiclist(searchList:ArrayList<Music>)
    {
        musicList= ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }
    private fun sendIntent(ref:String,pos:Int)
    {
        val intent= Intent(context,PlayerActivity::class.java)
        intent.putExtra("index",pos)
        intent.putExtra("class",ref)
        ContextCompat.startActivity(context,intent,null)
    }
}