package com.example.magnifyingglass

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var backIcon: ImageView
    private lateinit var cameraSoundCard: ImageView
    private lateinit var focusModeCard: ImageView
    private lateinit var singleTopCard: ImageView
    private lateinit var shareAppCard: ImageView
    private var popupWindow: PopupWindow? = null
    private lateinit var dropDownIconCameraSound : ImageView
    private lateinit var dropDownIconFocusMode : ImageView
    private lateinit var dropDownIconSingleTop : ImageView
    private var playShutterSound = false
    private var videoModeSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentInstance = this
        setContentView(R.layout.activity_settings)

        cameraSoundCard = findViewById(R.id.settings_screen_card_camera_sound)
        focusModeCard = findViewById(R.id.settings_screen_card_focus_mode)
        singleTopCard = findViewById(R.id.settings_screen_card_single_top)
        shareAppCard = findViewById(R.id.settings_screen_card_share_app)
        dropDownIconCameraSound = findViewById(R.id.settings_screen_drop_down_icon)
        dropDownIconFocusMode = findViewById(R.id.settings_screen_drop_down_icon_focus_mode)
        dropDownIconSingleTop = findViewById(R.id.settings_screen_drop_down_icon_single_top)

        // Retrieve the state of the camera sound icon
        val sharedPreferencesCameraSound = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isCameraSoundOn = sharedPreferencesCameraSound.getBoolean("isCameraSoundOn", false)
        if (isCameraSoundOn) {
            dropDownIconCameraSound.setImageResource(R.drawable.on_icon_settings_screen)
        } else {
            dropDownIconCameraSound.setImageResource(R.drawable.off_icon_settings_screen)
        }

        // Retrieve the state of the focus mode icon
        val sharedPreferencesFocusMode = getSharedPreferences("settingsVideo", Context.MODE_PRIVATE)
        val isVideoModeOn = sharedPreferencesFocusMode.getBoolean("isVideoModeOn", false)
        if (isVideoModeOn) {
            dropDownIconFocusMode.setImageResource(R.drawable.video_icon_settings_screen)
        } else {
            dropDownIconFocusMode.setImageResource(R.drawable.picture_icon_settings_screen)
        }

        val sharedPreferences = getSharedPreferences("settingsSingleTop", Context.MODE_PRIVATE)
        val isSingleTopFocusOrNone = sharedPreferences.getBoolean("isSingleTopFocusOrNone", false)

        if (isSingleTopFocusOrNone){
            dropDownIconSingleTop.setImageResource(R.drawable.none_icon_settings_screen)
        }else{
            dropDownIconSingleTop.setImageResource(R.drawable.focus_icon_settings_screen)
        }

        cameraSoundCard.setOnClickListener {
            showDropDownMenu(cameraSoundCard)
        }

        focusModeCard.setOnClickListener {
            showDropDownMenuFocusMode(focusModeCard)
        }

        singleTopCard.setOnClickListener {
            showDropDownMenuSingleTop(singleTopCard)
        }

        backIcon = findViewById(R.id.back_icon_settings_screen)
        backIcon.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun showDropDownMenu(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_menu_camera_sound, null)
        val onText = popupView.findViewById<TextView>(R.id.text_on)
        val offText = popupView.findViewById<TextView>(R.id.text_off)

        val sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isCameraSoundOn = sharedPreferences.getBoolean("isCameraSoundOn", false)

        if (isCameraSoundOn) {
            dropDownIconCameraSound.setImageResource(R.drawable.on_icon_settings_screen)
        } else {
            dropDownIconCameraSound.setImageResource(R.drawable.off_icon_settings_screen)
        }

        onText.setOnClickListener {
            dropDownIconCameraSound.setImageResource(R.drawable.on_icon_settings_screen)
            sharedPreferences.edit().putBoolean("isCameraSoundOn", true).apply()
            currentInstance?.playShutterSound = true // Set the flag to true if "On" is selected
            dismissDropDownMenu()
        }
        offText.setOnClickListener {
            dropDownIconCameraSound.setImageResource(R.drawable.off_icon_settings_screen)
            sharedPreferences.edit().putBoolean("isCameraSoundOn", false).apply()
            currentInstance?.playShutterSound = false // Set the flag to false if "Off" is selected
            dismissDropDownMenu()
        }

        // Create a PopupWindow and show it
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Calculate the x and y offsets to display the popup at the right side of the anchorView
        val xOffset = anchorView.width - 130  // Adjust this value as needed
        val yOffset = -30                  // Adjust this value as needed

        popupWindow?.showAsDropDown(anchorView, xOffset, yOffset)
    }

    private fun showDropDownMenuFocusMode(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_menu_focus_mode, null)
        val pictureText = popupView.findViewById<TextView>(R.id.picture_tv)
        val videoText = popupView.findViewById<TextView>(R.id.video_tv)

        val sharedPreferences = getSharedPreferences("settingsVideo", Context.MODE_PRIVATE)
        val isVideoModeOn = sharedPreferences.getBoolean("isVideoModeOn", false)

        if (isVideoModeOn){
            dropDownIconFocusMode.setImageResource(R.drawable.video_icon_settings_screen)
        }else{
            dropDownIconFocusMode.setImageResource(R.drawable.picture_icon_settings_screen)
        }

        // Set click listeners for the menu items
        pictureText.setOnClickListener {
            dropDownIconFocusMode.setImageResource(R.drawable.picture_icon_settings_screen)
            sharedPreferences.edit().putBoolean("isVideoModeOn", false).apply()
            currentInstance?.videoModeSelected = false
            dismissDropDownMenu()
        }
        videoText.setOnClickListener {
            dropDownIconFocusMode.setImageResource(R.drawable.video_icon_settings_screen)
            sharedPreferences.edit().putBoolean("isVideoModeOn", true).apply()
            currentInstance?.videoModeSelected = true
            dismissDropDownMenu()
        }

        // Create a PopupWindow and show it
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Calculate the x and y offsets to display the popup at the right side of the anchorView
        val xOffset = anchorView.width - 160  // Adjust this value as needed
        val yOffset = -30                  // Adjust this value as needed

        popupWindow?.showAsDropDown(anchorView, xOffset, yOffset)
    }

    private fun showDropDownMenuSingleTop(anchorView: View) {
        val popupView = layoutInflater.inflate(R.layout.popup_menu_single_top, null)
        val noneText = popupView.findViewById<TextView>(R.id.none_tv)
        val focusText = popupView.findViewById<TextView>(R.id.focus_tv)

        val sharedPreferences = getSharedPreferences("settingsSingleTop", Context.MODE_PRIVATE)
        val isSingleTopFocusOrNone = sharedPreferences.getBoolean("isSingleTopFocusOrNone", false)

        if (isSingleTopFocusOrNone){
            dropDownIconSingleTop.setImageResource(R.drawable.none_icon_settings_screen)
        }else{
            dropDownIconSingleTop.setImageResource(R.drawable.focus_icon_settings_screen)
        }

        // Set click listeners for the menu items
        noneText.setOnClickListener {
            dropDownIconSingleTop.setImageResource(R.drawable.none_icon_settings_screen)
            dismissDropDownMenu()
        }
        focusText.setOnClickListener {
            dropDownIconSingleTop.setImageResource(R.drawable.focus_icon_settings_screen)
            dismissDropDownMenu()
        }

        // Create a PopupWindow and show it
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Calculate the x and y offsets to display the popup at the right side of the anchorView
        val xOffset = anchorView.width - 160  // Adjust this value as needed
        val yOffset = -30                  // Adjust this value as needed

        popupWindow?.showAsDropDown(anchorView, xOffset, yOffset)
    }

    private fun dismissDropDownMenu() {
        popupWindow?.dismiss()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    companion object {
        var currentInstance: SettingsActivity? = null
    }
}