package com.muviplayer.android

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muviplayer.android.databinding.MusicViewBinding

class MusicAdapter(
    private val context: Context,
    private var musicList: ArrayList<Music>,
    private val playlistDetails: Boolean = false,
    private val selectionActivity: Boolean = false) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    class MyHolder(binding: MusicViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        val frame = binding.frameLayout
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = formatArtist(musicList[position].artist)
        holder.duration.text = formatDuration(musicList[position].duration)

        val albumArt = getAlbumArt(musicList[position].path)
        if (albumArt != null)
            Glide.with(context)
                .asBitmap()
                .load(albumArt)
                .into(holder.image)
        else
            Glide.with(context)
                .asBitmap()
                .load(R.drawable.music_cover)
                .into(holder.image)

        when{
            playlistDetails ->{
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }
            }
            selectionActivity ->{
                holder.root.setOnClickListener {
                    if(addSong(musicList[position]))
                        holder.frame.setBackgroundColor(ContextCompat.getColor(context, R.color.musicBackgroundSelected))
                    else
                        holder.frame.setBackgroundColor(ContextCompat.getColor(context, R.color.musicBackground))
                }
            }
            else ->{
                holder.root.setOnClickListener {
                    when{
                        MainActivity.searchMusic -> searchIntent(pos = position)

                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                        else->sendIntent(ref="MusicAdapter", pos = position)
                    }
                }
            }
        }
    }

    private fun searchIntent(pos: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", "MusicAdapterSearch")
        intent.putExtra("currentPosition", pos)
        ContextCompat.startActivity(context, intent, null)
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    fun updateMusicList(musicSearchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(musicSearchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun addSong(song: Music): Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if(song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}