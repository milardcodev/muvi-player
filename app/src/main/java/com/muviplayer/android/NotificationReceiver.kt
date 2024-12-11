package com.muviplayer.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            ApplicationClass.PREVIOUS ->
                if (PlayerActivity.musicListPA.size > 1) prevNextSong(
                increment = false, context = context!!)

            ApplicationClass.PLAY ->
                if (PlayerActivity.isPlaying)
                    pauseMusic()
                else
                    playMusic()

            ApplicationClass.NEXT ->
                if (PlayerActivity.musicListPA.size > 1) prevNextSong(
                increment = true, context = context!!)
        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.ic_pause)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.ic_play)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
    }

    private fun prevNextSong(increment: Boolean, context: Context){
        setSongPosition(increment = increment)
        PlayerActivity.musicService!!.createMediaPlayer()
        val albumArt = getAlbumArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        if (albumArt != null)
            Glide.with(context)
                .asBitmap()
                .load(albumArt)
                .into(PlayerActivity.binding.songImgPA)
        else
            Glide.with(context)
                .asBitmap()
                .load(R.drawable.music_cover)
                .into(PlayerActivity.binding.songImgPA)

        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.binding.songAlbumPA.text = formatArtist(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
        playMusic()
        PlayerActivity.fIndex = favouriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if(PlayerActivity.isFavourite)
            PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.ic_favourite_red)
        else
            PlayerActivity.binding.favouriteBtnPA.setImageResource(R.drawable.ic_favourite)
    }
}