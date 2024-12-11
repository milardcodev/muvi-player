package com.muviplayer.android

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.muviplayer.android.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean = false
        var shuffle: Boolean = false
        var nowPlayingId: String = ""
        var isFavourite: Boolean = false
        var fIndex: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeLayout()

        binding.backBtnPA.setOnClickListener {
            finish()
        }

        binding.playPauseBtnPA.setOnClickListener{
            if(isPlaying)
                pauseMusic()
            else
                playMusic()
        }

         binding.previousBtnPA.setOnClickListener {
             prevNextSong(increment = false)
         }

        binding.nextBtnPA.setOnClickListener {
            prevNextSong(increment = true)
        }

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    musicService!!.mediaPlayer!!.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })

        binding.repeatBtnPA.setOnClickListener {
            if(!repeat){
                repeat = true
                binding.repeatBtnPA.setIconResource(R.drawable.ic_repeat_on)
            }else{
                repeat = false
                binding.repeatBtnPA.setIconResource(R.drawable.ic_repeat_off)
            }
        }

        binding.shuffleBtnPA.setOnClickListener {
            if(!shuffle){
                shuffle = true
                binding.shuffleBtnPA.setIconResource(R.drawable.ic_shuffle_on)
            }else{
                shuffle = false
                binding.shuffleBtnPA.setIconResource(R.drawable.ic_shuffle_off)
            }
        }

        binding.favouriteBtnPA.setOnClickListener {
            fIndex = favouriteChecker(musicListPA[songPosition].id)
            if(isFavourite){
                isFavourite = false
                binding.favouriteBtnPA.setImageResource(R.drawable.ic_favourite)
                FavouriteActivity.favouriteSongs.removeAt(fIndex)
            }
            else
            {
                isFavourite = true
                binding.favouriteBtnPA.setImageResource(R.drawable.ic_favourite_red)
                FavouriteActivity.favouriteSongs.add(musicListPA[songPosition])
            }
            FavouriteActivity.favouritesChanged = true
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Sharing Music File!!"))
        }
    }

    private fun setLayout() {
        fIndex = favouriteChecker(musicListPA[songPosition].id)
        binding.songNamePA.isSelected = true

        val albumArt = getAlbumArt(musicListPA[songPosition].path)
        if (albumArt != null)
            Glide.with(this)
                .asBitmap()
                .load(albumArt)
                .into(binding.songImgPA)
        else
            Glide.with(this)
                .asBitmap()
                .load(R.drawable.music_cover)
                .into(binding.songImgPA)

        binding.songNamePA.text = musicListPA[songPosition].title
        binding.songAlbumPA.text = formatArtist(musicListPA[songPosition].artist)

        if (repeat)
            binding.repeatBtnPA.setIconResource(R.drawable.ic_repeat_on)

        if (shuffle)
            binding.shuffleBtnPA.setIconResource(R.drawable.ic_shuffle_on)

        if (isFavourite)
            binding.favouriteBtnPA.setImageResource(R.drawable.ic_favourite_red)
        else
            binding.favouriteBtnPA.setImageResource(R.drawable.ic_favourite)

        val art = getAlbumArt(musicListPA[songPosition].path)
        if (art != null) {
            val bitmap = BitmapFactory.decodeByteArray(art, 0, art.size)
            applyPalette(bitmap)
        } else {
            Glide.with(this).asBitmap().load(R.drawable.music_cover).into(binding.songImgPA)
            applyDefaultPalette()
        }
    }

    private fun applyPalette(bitmap: Bitmap) {
        ImageAnimation(this, binding.songImgPA, bitmap)
        Palette.from(bitmap).generate { palette ->
            val swatch: Palette.Swatch? = palette?.dominantSwatch
            val backgroundColor = swatch?.rgb ?: -0xFFFFFF
            setBackgroundAndColors(backgroundColor)
            setIconTint(backgroundColor)
        }
    }

    private fun ImageAnimation(context: Context, imageView: ImageView, bitmap: Bitmap) {
        val animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
        val animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
        animOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                Glide.with(context).load(bitmap).into(imageView)
                imageView.startAnimation(animIn)
            }

            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
        })
        imageView.startAnimation(animOut)
    }

    private fun applyDefaultPalette() {
        val mContainer = findViewById<ConstraintLayout>(R.id.media_player_control_container)
        mContainer.setBackgroundColor(Color.parseColor("#778195"))
        setIconTint(Color.parseColor("#778195"))
        setStatusBarAndNavigationBarColor(Color.parseColor("#778195"))
    }

    private fun setBackgroundAndColors(backgroundColor: Int) {
        val mContainer = findViewById<ConstraintLayout>(R.id.media_player_control_container)
        val gradiantDrawableBg = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(backgroundColor, backgroundColor)
        )
        mContainer.background = gradiantDrawableBg
        setStatusBarAndNavigationBarColor(backgroundColor)
    }

    private fun setIconTint(color: Int) {
        val buttonIds = listOf(
            R.id.repeatBtnPA,
            R.id.previousBtnPA,
            R.id.playPauseBtnPA,
            R.id.nextBtnPA,
            R.id.shuffleBtnPA)

        buttonIds.forEach { buttonId ->
            val button = findViewById<ExtendedFloatingActionButton>(buttonId)
            button.iconTint = ColorStateList.valueOf(if (color != -0xFFFFFF) color else Color.parseColor("#778195"))
        }
    }

    private fun setStatusBarAndNavigationBarColor(color: Int) {
        val window = window
        window.statusBarColor = color
        window.navigationBarColor = color
    }

    private fun createMediaPlayer(){
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.ic_pause)
            musicService!!.showNotification(R.drawable.ic_pause)
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id

            NowPlaying.binding.linearProgressIndicatorNP.progress = 0
            NowPlaying.binding.linearProgressIndicatorNP.max = musicService!!.mediaPlayer!!.duration
        }
        catch (e: Exception) {
            return
        }
    }

    private fun initializeLayout(){
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){
            "NowPlaying"-> {
                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration

                NowPlaying.binding.linearProgressIndicatorNP.progress = musicService!!.mediaPlayer!!.currentPosition
                NowPlaying.binding.linearProgressIndicatorNP.max = musicService!!.mediaPlayer!!.duration
                if (isPlaying)
                    binding.playPauseBtnPA.setIconResource(R.drawable.ic_pause)
                else
                    binding.playPauseBtnPA.setIconResource(R.drawable.ic_play)
            }

            "MusicAdapterSearch" -> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }

            "MusicAdapter" -> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                setLayout()
            }

            "FavouriteAdapter"-> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                setLayout()
            }

            "FavouriteShuffle"-> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(FavouriteActivity.favouriteSongs)
                musicListPA.shuffle()
                setLayout()
            }

            "PlaylistDetailsAdapter"-> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                setLayout()
            }

            "PlaylistDetailsShuffle"-> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }

            "MainActivity" -> {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                setLayout()
            }
        }
    }

    private fun playMusic(){
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnPA.setIconResource(R.drawable.ic_pause)
        musicService!!.showNotification(R.drawable.ic_pause)
    }

    private fun pauseMusic(){
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnPA.setIconResource(R.drawable.ic_play)
        musicService!!.showNotification(R.drawable.ic_play)
    }

    private fun prevNextSong(increment: Boolean){
        if(increment)
        {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        if(musicService == null){
            val binder = service as MusicService.MyBinder
            musicService = binder.currentService()
            musicService!!.audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        }
        createMediaPlayer()
        musicService!!.seekBarSetup()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        setLayout()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 13 || resultCode == RESULT_OK)
            return
    }
}