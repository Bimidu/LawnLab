package com.example.madlabexam03

import android.animation.ObjectAnimator
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hiding the status bar and action bar appropriately based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            actionBar?.hide()
        }

        // Animate multiple images
        animateImage(R.id.sidemowerimg1)
        animateImage(R.id.sidemowerimg2)
        animateImage(R.id.sidemowerimg3)
        animateImage(R.id.sidemowerimg4)
        animateImage(R.id.sidemowerimg5)
        animateImage(R.id.sidemowerimg6)
    }

    private fun animateImage(imageViewId: Int) {
        val imageView = findViewById<ImageView>(imageViewId)

        imageView.visibility = View.INVISIBLE

        imageView.post {
            val imageWidth = imageView.width.toFloat()
            val screenWidth = resources.displayMetrics.widthPixels.toFloat()

            // Generate random values for duration, delay, and starting position using Random
            val duration = Random.nextLong(1000L, 4000L) // Random duration between 1 to 4 seconds
            val delay = Random.nextLong(0L, 2000L) // Random delay between 0 to 2 seconds
            val startPosition = -imageWidth  // Start fully off-screen to the left

            // Calculate the end position based on the starting position and screen width
            val endPosition = startPosition + screenWidth + imageWidth

            // Create the animator with random values
            val animator = ObjectAnimator.ofFloat(
                imageView,
                "translationX",
                startPosition,
                endPosition
            )

            animator.duration = duration // Set random duration
            animator.startDelay = delay // Set random delay before starting
            animator.repeatCount = ObjectAnimator.INFINITE // Repeat animation indefinitely
            animator.interpolator = LinearInterpolator() // Maintain constant speed

            // Set the ImageView to visible right before animation starts
            imageView.visibility = View.VISIBLE

            animator.start() // Start the animation
        }
    }


}
