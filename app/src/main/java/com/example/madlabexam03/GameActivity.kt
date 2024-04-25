package com.example.madlabexam03

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import com.google.android.material.internal.ViewUtils.dpToPx


class GameActivity : AppCompatActivity() {
    private val NUM_COLUMNS = 20 // Number of columns in the grid
    private val NUM_ROWS = 10
    private val TILE_MARGIN_DP = 0.5 // Margin between tiles in dp
    private lateinit var mowerImageView: ImageView // Reference to the mower ImageView

    private lateinit var gridLayout: GridLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Hiding the status bar and action bar appropriately based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
        }

        hideNavigationBar()

        val gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // Get reference to the mower ImageView
        mowerImageView = findViewById(R.id.mower)

        // Calculate tile size
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val tileSize = screenWidth / NUM_COLUMNS
        val tileMargin = dpToPx(TILE_MARGIN_DP)

        val oneTwentiethScreenHeight = screenWidth / 20

        val mowerImageView = findViewById<ImageView>(R.id.mower)
        val layoutParams = mowerImageView.layoutParams
        layoutParams.width = oneTwentiethScreenHeight
        mowerImageView.layoutParams = layoutParams

        // Add tiles dynamically
        for (i in 0 until NUM_COLUMNS * NUM_ROWS) {
            val imageView = ImageView(this)
            val tileParams = GridLayout.LayoutParams()
            tileParams.width = tileSize
            tileParams.height = tileSize
            tileParams.setMargins(tileMargin, tileMargin, tileMargin, tileMargin) // Set margins
            imageView.layoutParams = tileParams
            imageView.setBackgroundColor(ContextCompat.getColor(this, R.color.grass_green1)) // Set tile color

            // Add click listener to each tile
            imageView.setOnClickListener {
                // Calculate the position where the mower should move
                val column = i % NUM_COLUMNS
                val row = i / NUM_COLUMNS
                moveMowerToPosition(column, row)
            }

            gridLayout.addView(imageView)
        }

        // Black out remaining space
        val blackView = View(this)
        blackView.setBackgroundColor(Color.BLACK)
        val blackParams = GridLayout.LayoutParams()
        blackParams.width = screenWidth - (NUM_COLUMNS * tileSize)
        blackParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        blackView.layoutParams = blackParams
        gridLayout.addView(blackView)
    }




    private fun dpToPx(dp: Double): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.decorView.apply {
                systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN)
            }
        }
    }

    private fun moveMowerToPosition(column: Int, row: Int) {
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels

        val oneTwentiethScreenHeight = screenWidth / 20

        // Calculate the new position for the mower ImageView
        val newX = column * oneTwentiethScreenHeight + oneTwentiethScreenHeight / 2
        val newY = row * oneTwentiethScreenHeight + oneTwentiethScreenHeight / 2

        // Calculate the offset to center the mower ImageView within the tile
        val mowerHalfWidth = mowerImageView.width / 2
        val mowerHalfHeight = mowerImageView.height / 2

        // Adjust the position to center the mower ImageView within the tile
        val adjustedX = newX - mowerHalfWidth
        val adjustedY = newY - mowerHalfHeight

        // Use ViewPropertyAnimator to animate the position
        mowerImageView.animate()
            .x(adjustedX.toFloat())
            .y(adjustedY.toFloat())
            .setDuration(500) // Set the duration of the animation (in milliseconds)
            .setInterpolator(AccelerateDecelerateInterpolator()) // Set the interpolator

            .start() // Start the animation
    }


}
