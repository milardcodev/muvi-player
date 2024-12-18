package com.muviplayer.android

import java.util.concurrent.TimeUnit

data class Video(
    var path: String,
    var name: String,
    var artist: String,
    var album: String,
    val duration: Long = 0,
    var id: String,
    var size: String)



fun formattedDuration(duration: Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}