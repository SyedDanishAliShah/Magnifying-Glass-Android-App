package com.example.magnifyingglass

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class ImageDisplayActivity : AppCompatActivity() {

    private lateinit var capturedImageView: ImageView
    private lateinit var backIcon : ImageView
    private lateinit var shareButton : ImageView
    private lateinit var deleteButton : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_display)


        capturedImageView = findViewById(R.id.captured_image)
        backIcon = findViewById(R.id.back_icon_display_image_screen)
        shareButton = findViewById(R.id.share_button_display_screen)
        deleteButton = findViewById(R.id.delete_button_display_screen)


        shareButton.setOnClickListener {
            val imageUri = intent.getStringExtra("imageUri")
            val file = File(Uri.parse(imageUri).path ?: "")
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "No suitable app found", Toast.LENGTH_SHORT).show()
            }
        }


        backIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        deleteButton.setOnClickListener {
            val imageUri = intent.getStringExtra("imageUri")
            val imageFile = Uri.parse(imageUri).path?.let { it1 -> File(it1) }
            if (imageFile != null) {
                if (imageFile.exists()) {
                    imageFile.delete()
                    Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
        val imageUri = intent.getStringExtra("imageUri")
        capturedImageView.setImageURI(Uri.parse(imageUri))

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}