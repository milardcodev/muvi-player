package com.muviplayer.android

import android.content.Context
import android.graphics.Color
import android.media.MediaMetadataRetriever
import androidx.appcompat.app.AlertDialog
import com.google.android.material.color.MaterialColors
import com.muviplayer.android.PlayerActivity.Companion.shuffle
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(
    val id:String,
    val title:String,
    val album:String,
    val artist:String,
    val duration: Long = 0,
    val path: String,
    val artUri:String)

class Playlist{
    lateinit var name: String
    lateinit var path: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

fun formatDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getAlbumArt(uri: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(uri)
    return retriever.embeddedPicture
}

fun getImgArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun formatArtist(artist: String): String {
    return if (artist.isEmpty() || artist == "<unknown>")
        "Unknown Artist"
    else
        artist
}

fun setSongPosition(increment: Boolean){
    if(!PlayerActivity.repeat){
        if(increment)
        {
            if(PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = 0
            else ++PlayerActivity.songPosition
        }else{
            if(0 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size-1
            else --PlayerActivity.songPosition
        }
    }
}

fun exitApplication(){
    if(PlayerActivity.musicService != null){
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null}
    exitProcess(1)
}

fun favouriteChecker(id: String): Int{
    PlayerActivity.isFavourite = false
    FavouriteActivity.favouriteSongs.forEachIndexed { index, music ->
        if(id == music.id){
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.forEachIndexed { index, music ->
        val file = File(music.path)
        if(!file.exists())
            playlist.removeAt(index)
    }
    return playlist
}

fun setDialogBtnBackground(context: Context, dialog: AlertDialog){
    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.WHITE)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
        MaterialColors.getColor(context, R.attr.dialogTextColor, Color.WHITE)
    )

    dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.RED)
    )
    dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setBackgroundColor(
        MaterialColors.getColor(context, R.attr.dialogBtnBackground, Color.RED)
    )
}


