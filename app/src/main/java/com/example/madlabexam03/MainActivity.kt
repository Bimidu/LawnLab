package com.example.madlabexam03

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
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

        hideNavigationBar()

        // Animate all images
        animateImage(R.id.sidemowerimg1)
        animateImage(R.id.sidemowerimg2)
        animateImage(R.id.sidemowerimg3)
        animateImage(R.id.sidemowerimg4)
        animateImage(R.id.sidemowerimg5)
        animateImage(R.id.sidemowerimg6)

    }

    fun startGame(view: View) {
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }

    private fun animateImage(imageViewId: Int) {
        val imageView = findViewById<ImageView>(imageViewId)

        // Initially hide the ImageView
        imageView.visibility = View.INVISIBLE

        imageView.post {
            val imageWidth = imageView.width.toFloat()
            val screenWidth = resources.displayMetrics.widthPixels.toFloat()

            // Generate random values for duration, delay, and starting position using Random
            val duration = Random.nextLong(1000L, 4000L) // Random duration between 1 to 4 seconds
            val delay = Random.nextLong(0L, 2000L) // Random delay between 0 to 2 seconds
            val startPosition = -imageWidth //starting when the image is left to the screen

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

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    // Set the ImageView to visible only when the animation actually starts
                    imageView.visibility = View.VISIBLE
                }
            })

            animator.start() // Start the animation
        }


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

}
