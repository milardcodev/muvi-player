package com.muviplayer.android

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.muviplayer.android.databinding.VideoViewBinding
import java.io.File

class VideoAdapter(
    private val context: Context,
    private var videoList: ArrayList<Video>) : RecyclerView.Adapter<VideoAdapter.MyHolder>(){

    class MyHolder(binding: VideoViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val title = binding.videoNameWA
        val image = binding.videoImgWA
        val duration = binding.videoDurationWA
        val sizeWA = binding.videoSizeWA
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(VideoViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = videoList[position].name
        holder.duration.text = formattedDuration(videoList[position].duration)

        Glide.with(context)
            .load(File(videoList[position].path))
            .into(holder.image)

        val fileSize = videoList[position].size.toLong()
        when {
            fileSize < 1024 * 1024 -> {
                val fileSizeInKB = fileSize / 1024.0
                val formattedSizeInKB = "%.2f KB".format(fileSizeInKB)
                holder.sizeWA.text = formattedSizeInKB
            }

            fileSize < 1024 * 1024 * 1024 -> {
                val fileSizeInMB = fileSize / (1024 * 1024.0)
                val formattedSizeInMB = "%.2f MB".format(fileSizeInMB)
                holder.sizeWA.text = formattedSizeInMB
            }

            else -> {
                val fileSizeInGB = fileSize / (1024 * 1024 * 1024.0)
                val formattedSizeInGB = "%.2f GB".format(fileSizeInGB)
                holder.sizeWA.text = formattedSizeInGB
            }
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, VideoPlayerActivity::class.java)
            intent.putExtra("position", position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

    fun updateList(videoSearchList: ArrayList<Video>) {
        videoList = ArrayList()
        videoList.addAll(videoSearchList)
        notifyDataSetChanged()
    }
}