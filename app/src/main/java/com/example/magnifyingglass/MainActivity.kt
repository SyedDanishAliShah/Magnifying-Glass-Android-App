package com.example.magnifyingglass

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var seekBarLayout: ConstraintLayout
    private var isSeekBarLayoutVisible = true
    private lateinit var previewView: PreviewView
    private lateinit var cameraIcon : ImageView
    private var cameraProvider: ProcessCameraProvider? = null
    private var frontCamera: Camera? = null
    private var backCamera: Camera? = null
    private lateinit var flashLightIcon : ImageView
    private lateinit var freezeCameraIcon : ImageView
    private var isPreviewFrozen = false
    private lateinit var preview: Preview
    private lateinit var cameraSelector: CameraSelector
    private var currentZoomLevel = 1f
    private var initialFingerSpacing = -1f
    private lateinit var captureImageIcon : ImageView
    private lateinit var savedPicturesIcon : ImageView
    private lateinit var effectIcon : ImageView
    private lateinit var brightnessIcon : ImageView
    private lateinit var settingsIcon : ImageView
    private lateinit var redShadeEffect : ImageView
    private lateinit var lightGreenShadeEffect : ImageView
    private lateinit var brownShadeEffect : ImageView
    private lateinit var yellowShadeEffect : ImageView
    private lateinit var darkGreenShadeEffect : ImageView
    private lateinit var startTextView : TextView
    private lateinit var seekBar: SeekBar
    private lateinit var maximumTextView : TextView
    private lateinit var selectionIconForEffect : ImageView
    private lateinit var redShadeView : View
    private lateinit var lightGreenShadeView : View
    private lateinit var brownShadeView : View
    private lateinit var yellowShadeView: View
    private lateinit var darkGreenShadeView: View
    private lateinit var lowBrightnessIcon : ImageView
    private lateinit var highBrightnessIcon : ImageView

    @SuppressLint("ClickableViewAccessibility", "CutPasteId", "SetTextI18n", "InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        effectIcon = findViewById(R.id.effect_icon_main_screen)
        brightnessIcon = findViewById(R.id.brightness_icon_main_screen)
        settingsIcon = findViewById(R.id.settings_icon_main_screen)
        startTextView = findViewById(R.id.textView)
        seekBar = findViewById(R.id.seekBar)
        redShadeEffect = findViewById(R.id.red_shade_effect_image)
        lightGreenShadeEffect = findViewById(R.id.light_green_shade_effect)
        brownShadeEffect = findViewById(R.id.brown_shade_effect)
        yellowShadeEffect = findViewById(R.id.yellow_shade_effect)
        darkGreenShadeEffect = findViewById(R.id.dark_green_shade_effect)
        maximumTextView = findViewById(R.id._10x_tv)
        selectionIconForEffect = findViewById(R.id.selection_icon_for_effect)
        redShadeView = findViewById(R.id.red_shade_view)
        lightGreenShadeView = findViewById(R.id.light_green_shade_view)
        brownShadeView = findViewById(R.id.brown_shade_view)
        yellowShadeView = findViewById(R.id.yellow_shade_view)
        darkGreenShadeView = findViewById(R.id.dark_green_shade_view)
        lowBrightnessIcon = findViewById(R.id.brightness_low_icon)
        highBrightnessIcon = findViewById(R.id.brightness_high_icon)

        brightnessIcon.setOnClickListener {
            if (isSeekBarLayoutVisible) {
                seekBarLayout.visibility = View.GONE
            } else {
                seekBarLayout.visibility = View.VISIBLE
                seekBar.progress = 5 // Set the seek bar's progress to 5
                backCamera?.cameraControl?.setZoomRatio(1f) // Set the zoom level to 1x
                frontCamera?.cameraControl?.setZoomRatio(1f)
                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        val brightnessLevel = progress / 10f // Assuming 0.0 to 1.0 range
                        // Update the brightness level of the camera preview
                        setBrightnessLevel(brightnessLevel)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Not needed
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Not needed
                    }
                })
            }
            isSeekBarLayoutVisible = !isSeekBarLayoutVisible
            startTextView.visibility = View.INVISIBLE
            maximumTextView.visibility = View.INVISIBLE
            lowBrightnessIcon.visibility = View.VISIBLE
            highBrightnessIcon.visibility = View.VISIBLE
            redShadeEffect.visibility = View.INVISIBLE
            seekBar.visibility = View.VISIBLE
            lightGreenShadeEffect.visibility = View.INVISIBLE
            brownShadeEffect.visibility = View.INVISIBLE
            yellowShadeEffect.visibility = View.INVISIBLE
            darkGreenShadeEffect.visibility = View.INVISIBLE
        }

        settingsIcon.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }


        redShadeEffect.setOnClickListener {

            // Toggle the selection state
            val isSelected = redShadeEffect.tag as? Boolean ?: false
            redShadeEffect.tag = !isSelected

            // Update the visibility of the selection icon based on the selection state
            selectionIconForEffect.visibility = if (!isSelected) View.VISIBLE else View.GONE

            // Hide all other shade views
            lightGreenShadeView.visibility = View.INVISIBLE
            brownShadeView.visibility = View.INVISIBLE
            yellowShadeView.visibility = View.INVISIBLE
            darkGreenShadeView.visibility = View.INVISIBLE

            if (!isSelected) {
                // Show the red shade view
                redShadeView.visibility = View.VISIBLE

                // Position the selection icon on the selected effect
                val params = selectionIconForEffect.layoutParams as ConstraintLayout.LayoutParams
                params.topToTop = redShadeEffect.id
                params.startToStart = redShadeEffect.id
                params.bottomToBottom = redShadeEffect.id
                params.endToEnd = redShadeEffect.id
                selectionIconForEffect.layoutParams = params

            } else {
                // Hide the red shade view
                redShadeView.visibility = View.INVISIBLE
            }
        }

        lightGreenShadeEffect.setOnClickListener {
            // Toggle the selection state
            val isSelected = lightGreenShadeEffect.tag as? Boolean ?: false
            lightGreenShadeEffect.tag = !isSelected

            // Update the visibility of the selection icon based on the selection state
            selectionIconForEffect.visibility = if (!isSelected) View.VISIBLE else View.GONE

            // Hide all other shade views
            redShadeView.visibility = View.INVISIBLE
            brownShadeView.visibility = View.INVISIBLE
            yellowShadeView.visibility = View.INVISIBLE
            darkGreenShadeView.visibility = View.INVISIBLE

            if (!isSelected) {
                // Show the red shade view
                lightGreenShadeView.visibility = View.VISIBLE

                // Position the selection icon on the selected effect
                val params = selectionIconForEffect.layoutParams as ConstraintLayout.LayoutParams
                params.topToTop = lightGreenShadeEffect.id
                params.startToStart = lightGreenShadeEffect.id
                params.bottomToBottom = lightGreenShadeEffect.id
                params.endToEnd = lightGreenShadeEffect.id
                selectionIconForEffect.layoutParams = params


            } else {
                // Hide the red shade view
                lightGreenShadeView.visibility = View.INVISIBLE
            }
        }

        brownShadeEffect.setOnClickListener {

            // Toggle the selection state
            val isSelected = brownShadeEffect.tag as? Boolean ?: false
            brownShadeEffect.tag = !isSelected

            // Update the visibility of the selection icon based on the selection state
            selectionIconForEffect.visibility = if (!isSelected) View.VISIBLE else View.GONE

            // Hide all other shade views
            lightGreenShadeView.visibility = View.INVISIBLE
            redShadeView.visibility = View.INVISIBLE
            yellowShadeView.visibility = View.INVISIBLE
            darkGreenShadeView.visibility = View.INVISIBLE

            if (!isSelected) {
                // Show the red shade view
                brownShadeView.visibility = View.VISIBLE

                // Position the selection icon on the selected effect
                val params = selectionIconForEffect.layoutParams as ConstraintLayout.LayoutParams
                params.topToTop = brownShadeEffect.id
                params.startToStart = brownShadeEffect.id
                params.bottomToBottom = brownShadeEffect.id
                params.endToEnd = brownShadeEffect.id
                selectionIconForEffect.layoutParams = params

            } else {
                // Hide the red shade view
                brownShadeView.visibility = View.INVISIBLE
            }
        }

        yellowShadeEffect.setOnClickListener {

            // Toggle the selection state
            val isSelected = yellowShadeEffect.tag as? Boolean ?: false
            yellowShadeEffect.tag = !isSelected

            // Update the visibility of the selection icon based on the selection state
            selectionIconForEffect.visibility = if (!isSelected) View.VISIBLE else View.GONE

            // Hide all other shade views
            lightGreenShadeView.visibility = View.INVISIBLE
            brownShadeView.visibility = View.INVISIBLE
            redShadeView.visibility = View.INVISIBLE
            darkGreenShadeView.visibility = View.INVISIBLE

            if (!isSelected) {
                // Show the red shade view
                yellowShadeView.visibility = View.VISIBLE

                // Position the selection icon on the selected effect
                val params = selectionIconForEffect.layoutParams as ConstraintLayout.LayoutParams
                params.topToTop = yellowShadeEffect.id
                params.startToStart = yellowShadeEffect.id
                params.bottomToBottom = yellowShadeEffect.id
                params.endToEnd = yellowShadeEffect.id
                selectionIconForEffect.layoutParams = params

            } else {
                // Hide the red shade view
                yellowShadeView.visibility = View.INVISIBLE
            }
        }

        darkGreenShadeEffect.setOnClickListener {

            // Toggle the selection state
            val isSelected = darkGreenShadeEffect.tag as? Boolean ?: false
            darkGreenShadeEffect.tag = !isSelected

            // Update the visibility of the selection icon based on the selection state
            selectionIconForEffect.visibility = if (!isSelected) View.VISIBLE else View.GONE

            // Hide all other shade views
            lightGreenShadeView.visibility = View.INVISIBLE
            brownShadeView.visibility = View.INVISIBLE
            yellowShadeView.visibility = View.INVISIBLE
            redShadeView.visibility = View.INVISIBLE

            if (!isSelected) {
                // Show the red shade view
                darkGreenShadeView.visibility = View.VISIBLE

                // Position the selection icon on the selected effect
                val params = selectionIconForEffect.layoutParams as ConstraintLayout.LayoutParams
                params.topToTop = darkGreenShadeEffect.id
                params.startToStart = darkGreenShadeEffect.id
                params.bottomToBottom = darkGreenShadeEffect.id
                params.endToEnd = darkGreenShadeEffect.id
                selectionIconForEffect.layoutParams = params


            } else {
                // Hide the red shade view
                darkGreenShadeView.visibility = View.INVISIBLE
            }
        }

        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = "Taking picture..."

        backIcon = findViewById(R.id.back_icon_home_screen)
        seekBarLayout = findViewById(R.id.seekBarLayout)
        previewView = findViewById(R.id.previewView)
        cameraIcon = findViewById(R.id.camera_icon_home_screen)
        flashLightIcon = findViewById(R.id.flash_light_icon_home_screen)
        freezeCameraIcon = findViewById(R.id.freeze_camera_icon_home_screen)
        captureImageIcon = findViewById(R.id.capture_picture_icon_home_screen)
        savedPicturesIcon = findViewById(R.id.saved_pictures_icon_home_screen)

        savedPicturesIcon.setOnClickListener {
            val intent = Intent(this, SavedPicturesActivity::class.java).apply {
                putParcelableArrayListExtra("imageUris", ArrayList(capturedImageUris))
            }
            startActivity(intent)
            finish()
        }

        effectIcon.setOnClickListener {
            if (isSeekBarLayoutVisible) {
                seekBarLayout.visibility = View.GONE
                redShadeEffect.visibility = View.INVISIBLE
                lightGreenShadeEffect.visibility = View.INVISIBLE
                brownShadeEffect.visibility = View.INVISIBLE
                yellowShadeEffect.visibility = View.INVISIBLE
                darkGreenShadeEffect.visibility = View.INVISIBLE
            } else {
                seekBarLayout.visibility = View.VISIBLE
                    // Show the effects and hide other views
                    redShadeEffect.visibility = View.VISIBLE
                    lightGreenShadeEffect.visibility = View.VISIBLE
                    brownShadeEffect.visibility = View.VISIBLE
                    yellowShadeEffect.visibility = View.VISIBLE
                    darkGreenShadeEffect.visibility = View.VISIBLE
                    startTextView.visibility = View.INVISIBLE
                    seekBar.visibility = View.INVISIBLE
                    maximumTextView.visibility = View.INVISIBLE
                    lowBrightnessIcon.visibility = View.INVISIBLE
                    highBrightnessIcon.visibility = View.INVISIBLE
            }
            isSeekBarLayoutVisible = !isSeekBarLayoutVisible

        }

        captureImageIcon.setOnClickListener {
            val sharedPreferences = getSharedPreferences("settingsVideo", Context.MODE_PRIVATE)
            val isVideoModeOn = sharedPreferences.getBoolean("isVideoModeOn", false)

            if (isVideoModeOn) {
                Toast.makeText(this, "Can't take pictures while in VIDEO mode", Toast.LENGTH_SHORT).show()
                return@setOnClickListener  // Exit the click listener without further processing
            }

            // Create a new instance of dialogView
            val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)

            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setView(dialogView)
            alertDialogBuilder.setCancelable(false)
            val alertDialog = alertDialogBuilder.create()

            // Show the dialog
            alertDialog.show()

            captureImage()
        }

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val textView = findViewById<TextView>(R.id.textView)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calculate the value based on progress

                setBrightnessLevel(0.3f)
                currentZoomLevel = progress.toFloat()
                // Update the camera's zoom level
                backCamera?.cameraControl?.setZoomRatio(progress.toFloat())
                textView.text = "${progress}x"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Not needed
            }
        })

        preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        previewView.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    if (event.pointerCount == 2) {
                        initialFingerSpacing = getFingerSpacing(event)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (event.pointerCount == 2 && initialFingerSpacing != -1f) {
                        val newFingerSpacing = getFingerSpacing(event)
                        val zoomDifference = (newFingerSpacing - initialFingerSpacing) / 200f
                        initialFingerSpacing = newFingerSpacing

                        var newZoomLevel = currentZoomLevel + zoomDifference
                        if (newZoomLevel < 0f) {
                            newZoomLevel = 0f
                        } else if (newZoomLevel > 10f) {
                            newZoomLevel = 10f
                        }

                        // Calculate the progress based on the newZoomLevel
                        val progress = ((newZoomLevel) / 10f * (seekBar.max)).toInt()

                        // Update the seek bar's progress and text view's value
                        seekBar.progress = progress
                        textView.text = "${progress}x"

                        currentZoomLevel = newZoomLevel
                        backCamera?.cameraControl?.setZoomRatio(currentZoomLevel)
                    }
                }
                MotionEvent.ACTION_POINTER_UP -> {
                    initialFingerSpacing = -1f
                }
            }
            true
        }

        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        freezeCameraIcon.setOnClickListener {
            isPreviewFrozen = !isPreviewFrozen // Toggle the freeze state

            if (isPreviewFrozen) {
                // Unbind the preview use case to freeze the preview
                cameraProvider?.unbindAll()
                Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
            } else {
                // Rebind the preview use case to resume the preview
                backCamera?.let {
                    cameraProvider?.bindToLifecycle(this, cameraSelector, preview)
                    Toast.makeText(this, "Resumed", Toast.LENGTH_SHORT).show()

                    }
                }
            }

        flashLightIcon.setOnClickListener {
            val cameraInfo = backCamera?.cameraInfo
            if (cameraInfo?.torchState?.value == TorchState.ON) {
                backCamera?.cameraControl?.enableTorch(false)
            } else {
                backCamera?.cameraControl?.enableTorch(true)
            }
        }

        cameraIcon.setOnClickListener {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
            cameraProviderFuture.addListener({
                cameraProvider = cameraProviderFuture.get()

                // Unbind all use cases
                cameraProvider?.unbindAll()

                val newCameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                try {
                    backCamera = null
                    frontCamera = null
                    cameraSelector = newCameraSelector
                    backCamera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview)
                } catch (exc: Exception) {
                    Log.e(TAG, "Use case binding failed", exc)
                }

            }, ContextCompat.getMainExecutor(this))
        }

        backIcon.setOnClickListener {
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }

        val zoomIcon = findViewById<ImageView>(R.id.zoom_icon_home_screen)
        zoomIcon.setOnClickListener {
            if (isSeekBarLayoutVisible) {
                seekBarLayout.visibility = View.GONE
            } else {
                seekBarLayout.visibility = View.VISIBLE
                seekBar.progress = 0
                lowBrightnessIcon.visibility = View.INVISIBLE
                highBrightnessIcon.visibility = View.INVISIBLE

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                        setBrightnessLevel(0.3f)
                        // Calculate the value based on progress
                        currentZoomLevel = progress.toFloat()
                        // Update the camera's zoom level
                        backCamera?.cameraControl?.setZoomRatio(progress.toFloat())
                        textView.text = "${progress}x"
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Not needed
                    }

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Not needed
                    }
                })

            }
            isSeekBarLayoutVisible = !isSeekBarLayoutVisible

            redShadeEffect.visibility = View.INVISIBLE
            lightGreenShadeEffect.visibility = View.INVISIBLE
            brownShadeEffect.visibility = View.INVISIBLE
            yellowShadeEffect.visibility = View.INVISIBLE
            darkGreenShadeEffect.visibility = View.INVISIBLE
            startTextView.visibility = View.VISIBLE
            seekBar.visibility = View.VISIBLE
            selectionIconForEffect.visibility = View.INVISIBLE
            maximumTextView.visibility = View.VISIBLE

        }

        // Check for and request the CAMERA permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            // Permission has been granted, initialize the camera
            initializeCamera()
        }
        // Rest of your onCreate method...
    }


    private fun initializeCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                backCamera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

        backCamera?.cameraControl?.setZoomRatio(currentZoomLevel)
    }

    private fun setBrightnessLevel(brightnessLevel: Float) {
        val cameraControl = backCamera?.cameraControl
        val exposureCompensation = backCamera?.cameraInfo?.exposureState?.exposureCompensationRange

        if (exposureCompensation != null) {
            val min = exposureCompensation.lower
            val max = exposureCompensation.upper
            val value = min + (max - min) * brightnessLevel

            cameraControl?.setExposureCompensationIndex(value.toInt())
        }
    }

    private fun getFingerSpacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }


    private fun captureImage() {
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val photoFile: File = createImageFile()
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Bind the imageCapture use case to the lifecycle
        cameraProvider?.bindToLifecycle(this, cameraSelector, imageCapture)

        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val imageUri = Uri.fromFile(photoFile)
                capturedImageUris.add(imageUri)

                saveDataToSharedPreferences(capturedImageUris)

                val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
                val isCameraSoundOn = sharedPreferences.getBoolean("isCameraSoundOn", false)

                // Play the shutter sound if the flag is true
                if (isCameraSoundOn) {
                    val mediaPlayer = MediaPlayer.create(this@MainActivity, R.raw.camera_shutter_sound)
                    mediaPlayer.start()
                }

                val intent = Intent(this@MainActivity, SavedPicturesActivity::class.java).apply {
                }
                startActivity(intent)

                Toast.makeText(this@MainActivity, "Image Saved Successfully", Toast.LENGTH_SHORT).show()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File(storageDir, "JPEG_${timeStamp}.jpg")
    }

    private fun saveDataToSharedPreferences(imageUris: List<Uri>) {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("imageUris", imageUris.map { it.toString() }.toSet())
        editor.apply()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Camera and storage permissions have been granted, initialize the camera
                initializeCamera()
            } else {
                // Camera permission or storage permission has been denied, handle this situation
                Log.e(TAG, "Camera or storage permission denied")
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_CAPTURE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            val imageUri = saveImage(imageBitmap) // Save the image and get the URI
            val intent = Intent(this, ImageDisplayActivity::class.java).apply {
                putExtra("imageUri", imageUri.toString()) // Pass the image URI to ImageDisplayActivity
            }
            startActivity(intent)
        }
    }
    private fun saveImage(bitmap: Bitmap): Uri {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val imageFile = File(imagesDir, "image.jpg")

        FileOutputStream(imageFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        return Uri.fromFile(imageFile)
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 123
        private const val IMAGE_CAPTURE_REQUEST_CODE = 124
        var capturedImageUris: ArrayList<Uri> = ArrayList()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, SplashActivity::class.java)
        startActivity(intent)
        finish()
    }
}