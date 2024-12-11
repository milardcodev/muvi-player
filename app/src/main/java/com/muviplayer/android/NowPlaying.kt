package com.muviplayer.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.muviplayer.android.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {

    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying)
                pauseMusic()
            else
                playMusic()
        }

        binding.nextBtnNP.setOnClickListener {
            setSongPosition(increment = true)
            PlayerActivity.musicService!!.createMediaPlayer()

            val albumArt = getAlbumArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            if (albumArt != null)
                Glide.with(requireContext())
                    .asBitmap()
                    .load(albumArt)
                    .into(binding.songImgNP)
            else
                Glide.with(requireContext())
                    .asBitmap()
                    .load(R.drawable.music_cover)
                    .into(binding.songImgNP)

            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            binding.songAlbumNP.text = formatArtist(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)

            binding.linearProgressIndicatorNP.progress = 0
            binding.linearProgressIndicatorNP.max = PlayerActivity.musicService!!.mediaPlayer!!.duration

            PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
            playMusic()
        }

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true

            val albumArt = getAlbumArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            if (albumArt != null)
                Glide.with(requireContext())
                    .asBitmap()
                    .load(albumArt)
                    .into(binding.songImgNP)
            else
                Glide.with(requireContext())
                    .asBitmap()
                    .load(R.drawable.music_cover)
                    .into(binding.songImgNP)

            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            binding.songAlbumNP.text = formatArtist(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)

            binding.linearProgressIndicatorNP.progress = 0
            binding.linearProgressIndicatorNP.max = PlayerActivity.musicService!!.mediaPlayer!!.duration

            if(PlayerActivity.isPlaying)
                binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
            else
                binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
        }
    }

    private fun playMusic(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.ic_pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_pause)
    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.ic_play)
        PlayerActivity.musicService!!.showNotification(R.drawable.ic_play)
    }
}