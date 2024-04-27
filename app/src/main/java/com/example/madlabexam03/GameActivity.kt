package com.example.madlabexam03

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import kotlin.math.atan2
import kotlin.random.Random

// Define the GameActivity class which extends AppCompatActivity
class GameActivity : AppCompatActivity() {
    // Number of columns in the grid
    private val NUM_COLUMNS = 20
    // Number of rows in the grid
    private val NUM_ROWS = 9
    // Margin between tiles in dp
    private val TILE_MARGIN_DP = 0.5
    // Reference to the mower ImageView
    private lateinit var mowerImageView: ImageView
    // Reference to the GridLayout
    private lateinit var gridLayout: GridLayout
    // Map to store whether each tile is a rock or not
    private val isRockTile = mutableMapOf<Int, Boolean>()
    // Set to store indices of mowed tiles
    private val mowedTiles = mutableSetOf<Int>()
    // Current score
    private var score = 0
    // TextView to display the score
    private lateinit var scoreTextView: TextView
    // Current column of the mower
    private var currentColumn = 0
    // Current row of the mower
    private var currentRow = 0
    // SharedPreferences instance
    private lateinit var sharedPreferences: SharedPreferences
    // Key to store high score in SharedPreferences
    private val HIGH_SCORE_KEY = "high_score"

    private lateinit var progressBar: ProgressBar

    private var timer: CountDownTimer? = null

    // Called when the activity is starting
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

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Find DPad buttons by their IDs
        val btnUpLeft = findViewById<Button>(R.id.btn_up_left)
        val btnUp = findViewById<Button>(R.id.btn_up)
        val btnUpRight = findViewById<Button>(R.id.btn_up_right)
        val btnLeft = findViewById<Button>(R.id.btn_left)
        val btnCenter = findViewById<Button>(R.id.btn_center)
        val btnRight = findViewById<Button>(R.id.btn_right)
        val btnDownLeft = findViewById<Button>(R.id.btn_down_left)
        val btnDown = findViewById<Button>(R.id.btn_down)
        val btnDownRight = findViewById<Button>(R.id.btn_down_right)

        // Set click listeners for DPad buttons
        btnUpLeft.setOnClickListener { moveMowerToDirection(-1, -1) }
        btnUp.setOnClickListener { moveMowerToDirection(0, -1) }
        btnUpRight.setOnClickListener { moveMowerToDirection(1, -1) }
        btnLeft.setOnClickListener { moveMowerToDirection(-1, 0) }
        btnCenter.setOnClickListener { /* Center button, no movement */ }
        btnRight.setOnClickListener { moveMowerToDirection(1, 0) }
        btnDownLeft.setOnClickListener { moveMowerToDirection(-1, 1) }
        btnDown.setOnClickListener { moveMowerToDirection(0, 1) }
        btnDownRight.setOnClickListener { moveMowerToDirection(1, 1) }

        scoreTextView = findViewById(R.id.score)

        gridLayout = findViewById<GridLayout>(R.id.gridLayout)

        // Set initial position of the mower to top-left corner
        currentColumn = 0
        currentRow = 0

        // Find and initialize the ProgressBar
        progressBar = findViewById(R.id.progressBar)

        // Set initial progress to full
        progressBar.max = 200
        progressBar.progress = 200


        // Move the mower to the top-left corner
        moveMowerToColumnRow(0, 0)

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
            val (color, isRock) = generateRandomColor(i)
            imageView.setBackgroundColor(color) // Set random tile color
            isRockTile[i] = isRock // Store whether the tile is grass or rock

            // Check if the tile color is grass_green4 and add it to the set
            if (color == ContextCompat.getColor(this, R.color.grass_green4)) {
                mowedTiles.add(i)
                Log.d("GameActivity", "Tile $i set to green 4")
            }

            /*// Add click listener to each tile
            imageView.setOnClickListener {
                // Calculate the position where the mower should move
                val column = i % NUM_COLUMNS
                val row = i / NUM_COLUMNS
                moveMowerToPosition(column, row)
            }*/

            gridLayout.addView(imageView)
        }

        // Calculate high score from SharedPreferences or default to 0
        val highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)
        val highScoreTextView = findViewById<TextView>(R.id.highscore)
        highScoreTextView.text = "High Score: $highScore"

        // Black out remaining space
        val blackView = View(this)
        blackView.setBackgroundColor(Color.BLACK)
        val blackParams = GridLayout.LayoutParams()
        blackParams.width = screenWidth - (NUM_COLUMNS * tileSize)
        blackParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        blackView.layoutParams = blackParams
        gridLayout.addView(blackView)
    }



    private fun moveMowerToColumnRow(column: Int, row: Int) {
        // Calculate the index of the destination tile
        val index = row * NUM_COLUMNS + column

        val isRock = isRockTile[index] ?: return  // If the tile information is not found, exit the function
        if (isRock) {
            // The target tile is a rock tile, so the mower cannot move onto it
            return
        }

        // Check if the tile is a green 4 tile
        val isGreenFourTile = index in mowedTiles

        // Calculate the new position for the mower ImageView
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val oneTwentiethScreenHeight = screenWidth / 20
        val newX = column * oneTwentiethScreenHeight + oneTwentiethScreenHeight / 2
        val newY = row * oneTwentiethScreenHeight + oneTwentiethScreenHeight / 2
        val mowerHalfWidth = mowerImageView.width / 2
        val mowerHalfHeight = mowerImageView.height / 2
        val adjustedX = newX - mowerHalfWidth
        val adjustedY = newY - mowerHalfHeight


        // Use ViewPropertyAnimator to animate the position
        mowerImageView.animate()
            .x(adjustedX.toFloat())
            .y(adjustedY.toFloat())
            .setDuration(500)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .withEndAction {
                // Change the color of the tile when the mower reaches it
                val tileView = gridLayout.getChildAt(index)
                tileView?.setBackgroundColor(ContextCompat.getColor(this, R.color.grass_green4))

                // Increment the score if the mower moves onto a grass tile (green 1)
                if (!isGreenFourTile) {
                        score += 50
                        scoreTextView.text = "Score: $score"
                        tileView?.setOnClickListener(null) // Disable click listener for this tile
                } else {
                    // Decrement the score by 10 when moving onto a green 4 tile
                    score -= 10
                    scoreTextView.text = "Score: $score"
                }

                // Add the tile to the set of green 4 tiles
                mowedTiles.add(index)

                // Update the current column and row of the mower
                currentColumn = column
                currentRow = row



                // Calculate the new high score
                val highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)
                val newHighScore = if (score > highScore) score else highScore

                // Update high score TextView
                val highScoreTextView = findViewById<TextView>(R.id.highscore)
                highScoreTextView.text = "High Score: $newHighScore"

                // Update high score in SharedPreferences whenever the score changes
                updateHighScore()


            }
            .start()


    }

    private fun startCountdown() {
        // Start the timer for 2 seconds
        timer = object : CountDownTimer(2000, 1) {
            override fun onTick(millisUntilFinished: Long) {
                // Update the timer UI
                val progress = (millisUntilFinished / 10).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                // Timer finished, call backtomenu with "Game Over" message
                backToMenuWithMessage("Game Over - Time's up!")
            }
        }.start()
    }

    private fun backToMenuWithMessage(message: String) {
        // Show a toast message with the provided message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // Navigate back to the main menu
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun updateHighScore() {
        // Retrieve the stored high score from SharedPreferences
        val highScore = sharedPreferences.getInt(HIGH_SCORE_KEY, 0)

        // Check if the current score is higher than the stored high score
        if (score > highScore) {
            // Update the high score in SharedPreferences
            with(sharedPreferences.edit()) {
                putInt(HIGH_SCORE_KEY, score)
                apply()
            }

            // Show a toast message indicating the new high score
            Toast.makeText(this, "New Highscore !!!", Toast.LENGTH_SHORT).show()
        }
    }



    private fun generateRandomColor(tileIndex: Int): Pair<Int, Boolean> {
        val column = tileIndex % NUM_COLUMNS
        val row = tileIndex / NUM_COLUMNS

        // Ensure the first 4 tiles in columns 1, 2 and rows 1, 2 are not rock tiles
        if ((column == 0 || column == 1) && (row == 0 || row == 1)) {
            // Return only grass colors for these tiles
            return Pair(ContextCompat.getColor(this, R.color.grass_green1), false)
        }

        // Randomly choose between grass, second green, and rock colors based on a condition
        val randomValue = Random.nextFloat()
        return when {
            randomValue < 0.93f -> Pair(ContextCompat.getColor(this, R.color.grass_green1), false) // Primary grass color
            else -> Pair(ContextCompat.getColor(this, R.color.lawn_obstacle), true) // Rock color
        }
    }

    private fun dpToPx(dp: Double): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    private fun moveMowerToDirection(horizontal: Int, vertical: Int) {
        // Reset the progress bar
        progressBar.progress = progressBar.max

        // Cancel the previous timer if it exists
        timer?.cancel()

        // Start the countdown timer
        startCountdown()


        // Calculate the new column and row based on the current position and direction
        val newColumn = currentColumn + horizontal
        val newRow = currentRow + vertical

        // Check if the new column and row are within the screen boundaries and
        // also check for specific conditions for diagonal movements
        if (newColumn in 0 until NUM_COLUMNS && newRow in 0 until NUM_ROWS) {
            if (!(currentColumn == 0 && horizontal == -1) && // Not moving left from column 0
                !(currentColumn == NUM_COLUMNS - 1 && horizontal == 1) && // Not moving right from last column
                !(currentRow == 0 && vertical == -1) && // Not moving up from row 0
                !(currentRow == NUM_ROWS - 1 && vertical == 1) // Not moving down from last row
            ) {
                // Call moveMowerToColumnRow with the new column and row
                moveMowerToColumnRow(newColumn, newRow)
            }
        }
    }

    fun backtomenu(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    /*private fun moveMowerToPosition(column: Int, row: Int) {
            // Calculate the difference between the current and destination positions
            val columnDiff = column - currentColumn
            val rowDiff = row - currentRow

            // Determine the new column and row for the mower
            val newColumn = currentColumn + if (columnDiff != 0) columnDiff / Math.abs(columnDiff) else 0
            val newRow = currentRow + if (rowDiff != 0) rowDiff / Math.abs(rowDiff) else 0

            // Ensure the new column and row are within bounds
            val validColumn = newColumn.coerceIn(0, NUM_COLUMNS - 1)
            val validRow = newRow.coerceIn(0, NUM_ROWS - 1)

            // Move the mower to the valid column and row
            moveMowerToColumnRow(validColumn, validRow)
        }*/


}
