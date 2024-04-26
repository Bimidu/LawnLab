package com.example.madlabexam03

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.gridlayout.widget.GridLayout
import kotlin.random.Random


class GameActivity : AppCompatActivity() {
    private val NUM_COLUMNS = 20 // Number of columns in the grid
    private val NUM_ROWS = 9
    private val TILE_MARGIN_DP = 0.5 // Margin between tiles in dp
    private lateinit var mowerImageView: ImageView // Reference to the mower ImageView

    private lateinit var gridLayout: GridLayout

    private val isRockTile = mutableMapOf<Int, Boolean>()
    private val greenFourTiles = mutableSetOf<Int>()

    private var score = 0
    private lateinit var scoreTextView: TextView

    private var currentColumn = 0
    private var currentRow = 0

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

        // Move the mower to the top-left corner
        moveMowerToColumnRow(1, 1)

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
                greenFourTiles.add(i)
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

        // Black out remaining space
        val blackView = View(this)
        blackView.setBackgroundColor(Color.BLACK)
        val blackParams = GridLayout.LayoutParams()
        blackParams.width = screenWidth - (NUM_COLUMNS * tileSize)
        blackParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        blackView.layoutParams = blackParams
        gridLayout.addView(blackView)
    }


    private fun moveMowerToDirection(horizontal: Int, vertical: Int) {
        // Calculate the new column and row based on the current position and direction
        val newColumn = currentColumn + horizontal
        val newRow = currentRow + vertical

        // Call moveMowerToColumnRow with the new column and row
        moveMowerToColumnRow(newColumn, newRow)
    }

    private fun dpToPx(dp: Double): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }


    private fun moveMowerToPosition(column: Int, row: Int) {
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
        val isGreenFourTile = index in greenFourTiles

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
                    val tileColor = (gridLayout.getChildAt(index) as? ImageView)?.backgroundTintList?.defaultColor
                    if (tileColor == ContextCompat.getColor(this, R.color.grass_green1)) {
                        score += 50
                        scoreTextView.text = "Score: $score"
                        tileView?.setOnClickListener(null) // Disable click listener for this tile
                    }
                } else {
                    // Decrement the score by 10 when moving onto a green 4 tile
                    score -= 10
                    scoreTextView.text = "Score: $score"
                }

                // Add the tile to the set of green 4 tiles
                greenFourTiles.add(index)

                // Update the current column and row of the mower
                currentColumn = column
                currentRow = row
            }
            .start()
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





}
