package com.muviplayer.android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.muviplayer.android.databinding.ActivityWatchBinding
import java.io.File

class WatchActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityWatchBinding
    private lateinit var videoAdapter: VideoAdapter

    companion object{
        lateinit var videoListWA : ArrayList<Video>
        lateinit var videoListSearch : ArrayList<Video>
        var searchVideo: Boolean = false
        var sortVideoOrder: Int = 0
        val sortingVideoList = arrayOf(
            MediaStore.Video.Media.DATE_ADDED + " DESC", MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE + " DESC")
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWatchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeLayout()
        setSupportActionBar(findViewById(R.id.toolbarWA))

        binding.bottomNavigator.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.myMusic -> {
                    startActivity(Intent(this@WatchActivity, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.watch -> true
                else -> false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initializeLayout(){
        videoListWA = getAllVideo()
        binding.videoRV.setHasFixedSize(true)
        binding.videoRV.setItemViewCacheSize(13)

        binding.videoRV.layoutManager = LinearLayoutManager(this@WatchActivity)
        videoAdapter = VideoAdapter(this@WatchActivity, videoListWA)
        binding.videoRV.adapter = videoAdapter
    }

    @SuppressLint("Recycle", "Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllVideo(): ArrayList<Video> {
        val tempVideoList = ArrayList<Video>()
        val selectionVideo = "${MediaStore.Video.Media.MIME_TYPE}=? OR ${MediaStore.Video.Media.MIME_TYPE}=?"
        val selectionVideoArgs = arrayOf("video/mp4", "video/mpeg")

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.SIZE
        )

        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selectionVideo, selectionVideoArgs,
            sortingVideoList[sortVideoOrder], null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE)) ?: "Unknown"
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID)) ?: "Unknown"
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM)) ?: "Unknown"
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST)) ?: "Unknown"
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)) ?: "Unknown"

                    val video = Video(path = pathC, name = titleC, artist = artistC, album = albumC, duration = durationC, id = idC, size = sizeC)
                    val file = File(video.path)
                    if (file.exists())
                        tempVideoList.add(video)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return tempVideoList
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()

        val sortVideoEditor = getSharedPreferences("SORTINGVIDEO", MODE_PRIVATE)
        val sortVideoValue = sortVideoEditor.getInt("sortVideoOrder", 0)
        if(sortVideoOrder != sortVideoValue){
            sortVideoOrder = sortVideoValue
            videoListWA = getAllVideo()
            videoAdapter.updateList(videoListWA)
        }
        binding.bottomNavigator.selectedItemId = R.id.watch
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.video_menu, menu)
        val searchItem = menu?.findItem(R.id.searchVideo)
        val searchView = searchItem?.actionView as SearchView?
        searchView?.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        videoListSearch = ArrayList()
        if(newText != null){
            val userInput = newText.lowercase()
            for (video in videoListWA)
                if(video.name.lowercase().contains(userInput))
                    videoListSearch.add(video)
            searchVideo = true
            videoAdapter.updateList(videoSearchList = videoListSearch)
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.by_video_name -> {
                sortVideoOrder = 1
                updateVideoSortList()
                return true
            }
            R.id.by_video_date -> {
                sortVideoOrder = 0
                updateVideoSortList()
                return true
            }
            R.id.by_video_size -> {
                sortVideoOrder = 2
                updateVideoSortList()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun updateVideoSortList() {
        val sortVideoEditor = getSharedPreferences("SORTINGVIDEO", MODE_PRIVATE).edit()
        sortVideoEditor.putInt("sortVideoOrder", sortVideoOrder)
        sortVideoEditor.apply()

        videoListWA = getAllVideo()
        videoAdapter.updateList(videoListWA)
    }
}