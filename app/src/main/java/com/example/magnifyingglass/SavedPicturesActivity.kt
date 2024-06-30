package com.example.magnifyingglass

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.magnifyingglass.adapters.SavedImagesAdapter
import java.io.File

class SavedPicturesActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_pictures)

        backIcon = findViewById(R.id.back_icon_saved_images_screen)
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)

        backIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        val imageUris = loadImageUrisFromSharedPreferences() as ArrayList<Uri>
        imageUris.let {
            val nonDeletedUris = filterDeletedImages(it)
            val recyclerView = findViewById<RecyclerView>(R.id.saved_images_screen_rv)
            recyclerView.layoutManager = GridLayoutManager(this, 4)
            recyclerView.adapter = SavedImagesAdapter(nonDeletedUris).apply {
                setOnItemClickListener(object : SavedImagesAdapter.OnItemClickListener {
                    override fun onItemClick(uri: Uri) {
                        val intent = Intent(
                            this@SavedPicturesActivity,
                            ImageDisplayActivity::class.java
                        ).apply {
                            putExtra("imageUri", uri.toString())
                        }
                        startActivity(intent)
                    }

                    override fun onItemDelete(uri: Uri) {
                        // Implement delete functionality if needed
                    }
                })
            }
        }
    }

    private fun filterDeletedImages(imageUris: ArrayList<Uri>): ArrayList<Uri> {
        val nonDeletedUris = ArrayList<Uri>()
        for (uri in imageUris) {
            val imageFile = File(uri.path ?: "")
            if (imageFile.exists()) {
                nonDeletedUris.add(uri)
            }
        }
        return nonDeletedUris
    }

    private fun loadImageUrisFromSharedPreferences(): List<Uri> {
        val urisSet = sharedPreferences.getStringSet("imageUris", setOf()) ?: setOf()
        return urisSet.map { Uri.parse(it) }
    }


}