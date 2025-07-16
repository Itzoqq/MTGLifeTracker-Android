package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.util.isColorDark

/**
 * A custom [ConstraintLayout] that displays a grid of commander damage totals.
 *
 * This view dynamically generates a grid of TextViews and dividers to show how much
 * commander damage the current player has taken from every other player in the game.
 * The layout of the grid changes automatically based on the total number of players.
 * It also colors the background of each cell to match the opponent's profile color.
 */
class CommanderDamageSummaryView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // A list to hold the TextViews that display damage numbers.
    private var textViews = mutableListOf<TextView>()
    // A list to hold the Views used as dividers in the grid.
    private var dividers = mutableListOf<View>()
    // The player count for which the current layout was built. Used to avoid unnecessary redraws.
    private var currentPlayerCount = 0
    // The ordered list of players as they appear in the grid.
    private var playerGrid = listOf<Player>()
    // A Paint object used for drawing the colored backgrounds of each cell.
    private val backgroundPaint = Paint()

    /**
     * The main public method to update the contents and layout of the view.
     *
     * This method is called whenever the game state changes. It checks if the player
     * count has changed to determine if a full layout rebuild is necessary. It then
     * updates the text and color of each cell in the grid.
     *
     * @param currentPlayer The [Player] who owns the parent [PlayerSegmentView].
     * @param allPlayersInGame The complete list of all players in the current game.
     * @param damageToCurrentPlayer A list of [CommanderDamage] entries where the target is the [currentPlayer].
     * @param angle The rotation angle of the parent view, used to correctly rotate the text inside the cells.
     */
    fun updateView(
        currentPlayer: Player,
        allPlayersInGame: List<Player>,
        damageToCurrentPlayer: List<CommanderDamage>,
        angle: Int
    ) {
        Logger.d("SummaryView: updateView called for player ${currentPlayer.playerIndex}.")
        val playerCount = allPlayersInGame.size
        // If the player count has changed, we need to completely rebuild the grid layout.
        if (playerCount != currentPlayerCount) {
            Logger.i("SummaryView: Player count changed from $currentPlayerCount to $playerCount. Setting up new layout.")
            setupLayout(playerCount)
        }

        // Get the specific grid order for the current player count.
        playerGrid = getPlayerGrid(playerCount, allPlayersInGame)
        // Create a map for quick lookup of damage values by the source player's index.
        val damageMap = damageToCurrentPlayer.associateBy { it.sourcePlayerIndex }

        textViews.forEachIndexed { index, textView ->
            val sourcePlayer = playerGrid.getOrNull(index)
            if (sourcePlayer == null) {
                // This can happen in asymmetrical layouts (like 5 players). Hide the unused cell.
                textView.visibility = GONE
                return@forEachIndexed
            }
            Logger.d("SummaryView: Binding cell $index to player ${sourcePlayer.playerIndex} ('${sourcePlayer.name}').")

            textView.visibility = VISIBLE
            textView.rotation = angle.toFloat() // Rotate the text to match the parent segment.
            textView.background = null // Clear any old background; it will be drawn manually in onDraw.
            textView.alpha = 1.0f

            // Determine if the text should be light or dark based on the player's background color.
            val contrastColor = if (sourcePlayer.color != null && !isColorDark(sourcePlayer.color.toColorInt())) {
                Color.BLACK
            } else {
                Color.WHITE
            }
            textView.setTextColor(contrastColor)

            // If the cell represents the current player, show "Me" and disable it.
            if (sourcePlayer.playerIndex == currentPlayer.playerIndex) {
                textView.text = context.getString(R.string.me)
                textView.alpha = 0.6f
            } else {
                // Otherwise, display the commander damage from that opponent.
                textView.text = (damageMap[sourcePlayer.playerIndex]?.damage ?: 0).toString()
            }
        }
        // Trigger a redraw to render the new backgrounds set in onDraw.
        invalidate()
    }

    /**
     * Overridden to manually draw the colored background for each grid cell.
     *
     * Drawing the backgrounds manually in `onDraw` instead of setting them on the TextViews
     * gives more control and can be more performant, especially when dealing with many views
     * in a complex layout that redraws frequently.
     */
    override fun onDraw(canvas: Canvas) {
        Logger.d("SummaryView: onDraw called.")
        // Before drawing the TextViews, draw a colored rectangle behind each one.
        textViews.forEachIndexed { index, textView ->
            playerGrid.getOrNull(index)?.color?.let { colorString ->
                try {
                    backgroundPaint.color = colorString.toColorInt()
                    canvas.drawRect(
                        textView.left.toFloat(),
                        textView.top.toFloat(),
                        textView.right.toFloat(),
                        textView.bottom.toFloat(),
                        backgroundPaint
                    )
                } catch (e: Exception) {
                    Logger.e(e, "SummaryView: Failed to parse color string '$colorString' for player at grid index $index.")
                }
            }
        }
        // After drawing our custom backgrounds, call super.onDraw() to draw the children (the TextViews).
        super.onDraw(canvas)
    }

    /**
     * Determines the specific order of players for the grid layout.
     * For most player counts, this is a simple sort by index. For 5 players, it's a custom
     * order to match the 2x3 visual layout.
     */
    private fun getPlayerGrid(playerCount: Int, allPlayers: List<Player>): List<Player> {
        if (allPlayers.isEmpty()) return emptyList()
        val sortedPlayers = allPlayers.sortedBy { it.playerIndex }
        return when (playerCount) {
            5 -> {
                Logger.d("SummaryView: Using custom player grid order for 5 players.")
                listOf(sortedPlayers[0], sortedPlayers[2], sortedPlayers[1], sortedPlayers[3], sortedPlayers[4])
            }
            else -> sortedPlayers
        }
    }

    /**
     * Sets up the initial layout by creating all necessary TextViews and dividers and applying
     * the correct constraints for the given number of players.
     */
    private fun setupLayout(playerCount: Int) {
        // Clear all existing views from the layout.
        removeAllViews()
        textViews.clear()
        dividers.clear()
        if (playerCount < 2) {
            Logger.w("SummaryView: setupLayout called with player count < 2. Aborting.")
            return
        }

        // Create the required number of TextViews for the cells.
        (0 until playerCount).forEach { _ ->
            textViews.add(createCounterTextView())
        }
        Logger.d("SummaryView: Created ${textViews.size} TextViews for the grid.")

        // Apply the layout constraints.
        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        applyConstraints(constraintSet, playerCount)
        constraintSet.applyTo(this)
        currentPlayerCount = playerCount
        Logger.i("SummaryView: Layout setup complete for $playerCount players.")
    }

    /**
     * A dispatcher function that calls the correct constraint setup method based on player count.
     */
    private fun applyConstraints(cs: ConstraintSet, playerCount: Int) {
        Logger.d("SummaryView: Applying constraints for $playerCount players.")
        when (playerCount) {
            2 -> setup2PlayerConstraints(cs)
            3 -> setup3PlayerConstraints(cs)
            4 -> setup4PlayerConstraints(cs)
            5 -> setup5PlayerConstraints(cs)
            6 -> setup6PlayerConstraints(cs)
        }
    }

    /**
     * Factory method for creating a single TextView for a grid cell.
     */
    private fun createCounterTextView(): TextView {
        return TextView(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(0, 0) // Height and width are set by constraints.
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = Gravity.CENTER
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            addView(this) // Add the new view to this layout.
        }
    }

    /**
     * Factory method for creating a single divider view.
     */
    private fun addDivider(): View {
        return View(context).apply {
            id = generateViewId()
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            dividers.add(this)
            addView(this)
        }
    }

    // --- Constraint Setup Functions for each Player Count ---
    // These methods define the complex grid structure using ConstraintSet.

    private fun setup2PlayerConstraints(cs: ConstraintSet) {
        val hDivider = addDivider()
        cs.connect(hDivider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(hDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(hDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(hDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainHeight(hDivider.id, 1)
        cs.constrainWidth(hDivider.id, 0)

        val p1 = textViews[0]; val p2 = textViews[1]
        cs.connect(p1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.BOTTOM, hDivider.id, ConstraintSet.TOP)
        cs.connect(p2.id, ConstraintSet.TOP, hDivider.id, ConstraintSet.BOTTOM)
        cs.connect(p2.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        textViews.forEach {
            cs.connect(it.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            cs.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            cs.constrainPercentWidth(it.id, 1.0f)
            cs.constrainPercentHeight(it.id, 0.5f)
        }
    }

    private fun setup3PlayerConstraints(cs: ConstraintSet) {
        val hDivider = addDivider()
        val vDivider = addDivider()

        cs.connect(hDivider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        cs.connect(hDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        cs.connect(hDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        cs.connect(hDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        cs.constrainHeight(hDivider.id, 1)

        cs.constrainWidth(vDivider.id, 1)
        cs.constrainHeight(vDivider.id, 0)
        cs.connect(vDivider.id, ConstraintSet.TOP, hDivider.id, ConstraintSet.BOTTOM)
        cs.connect(vDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(vDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(vDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        val p1 = textViews[0]; val p2 = textViews[1]; val p3 = textViews[2]
        cs.connect(p1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.BOTTOM, hDivider.id, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(p1.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.connect(p2.id, ConstraintSet.TOP, hDivider.id, ConstraintSet.BOTTOM)
        cs.connect(p2.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(p2.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(p2.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        cs.connect(p3.id, ConstraintSet.TOP, hDivider.id, ConstraintSet.BOTTOM)
        cs.connect(p3.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(p3.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
        cs.connect(p3.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        cs.constrainPercentHeight(p1.id, 0.5f)
        cs.constrainPercentWidth(p2.id, 0.5f)
        cs.constrainPercentHeight(p2.id, 0.5f)
        cs.constrainPercentWidth(p3.id, 0.5f)
        cs.constrainPercentHeight(p3.id, 0.5f)
    }

    private fun setup4PlayerConstraints(cs: ConstraintSet) {
        val hDivider = addDivider()
        val vDivider = addDivider()
        cs.connect(hDivider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(hDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(hDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(hDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainHeight(hDivider.id, 1)
        cs.connect(vDivider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(vDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(vDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(vDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainWidth(vDivider.id, 1)

        val p1 = textViews[0]; val p2 = textViews[1]; val p3 = textViews[2]; val p4 = textViews[3]
        cs.connect(p1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.BOTTOM, hDivider.id, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(p1.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        cs.connect(p2.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p2.id, ConstraintSet.BOTTOM, hDivider.id, ConstraintSet.TOP)
        cs.connect(p2.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
        cs.connect(p2.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.connect(p3.id, ConstraintSet.TOP, hDivider.id, ConstraintSet.BOTTOM)
        cs.connect(p3.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(p3.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(p3.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        cs.connect(p4.id, ConstraintSet.TOP, hDivider.id, ConstraintSet.BOTTOM)
        cs.connect(p4.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(p4.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
        cs.connect(p4.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        textViews.forEach { cs.constrainPercentHeight(it.id, 0.5f); cs.constrainPercentWidth(it.id, 0.5f) }
    }

    private fun setup5PlayerConstraints(cs: ConstraintSet) {
        val vDivider = addDivider()
        val hDivLeft = addDivider()
        val hDivRight1 = addDivider()
        val hDivRight2 = addDivider()
        cs.connect(vDivider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(vDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(vDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(vDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainWidth(vDivider.id, 1)

        cs.connect(hDivLeft.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(hDivLeft.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        cs.connect(hDivLeft.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(hDivLeft.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.constrainHeight(hDivLeft.id, 1)

        listOf(hDivRight1, hDivRight2).forEach {
            cs.connect(it.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
            cs.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            cs.constrainHeight(it.id, 1)
        }
        cs.connect(hDivRight1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(hDivRight1.id, ConstraintSet.BOTTOM, hDivRight2.id, ConstraintSet.TOP)
        cs.connect(hDivRight2.id, ConstraintSet.TOP, hDivRight1.id, ConstraintSet.BOTTOM)
        cs.connect(hDivRight2.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.createVerticalChain(ConstraintSet.PARENT_ID, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, intArrayOf(hDivRight1.id, hDivRight2.id), null, ConstraintSet.CHAIN_SPREAD)

        val p1 = textViews[0]; val p2 = textViews[1]; val p3 = textViews[2]; val p4 = textViews[3]; val p5 = textViews[4]
        cs.connect(p1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.BOTTOM, hDivLeft.id, ConstraintSet.TOP)
        cs.connect(p3.id, ConstraintSet.TOP, hDivLeft.id, ConstraintSet.BOTTOM)
        cs.connect(p3.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        listOf(p1, p3).forEach {
            cs.connect(it.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            cs.connect(it.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        }
        cs.connect(p2.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p2.id, ConstraintSet.BOTTOM, hDivRight1.id, ConstraintSet.TOP)
        cs.connect(p4.id, ConstraintSet.TOP, hDivRight1.id, ConstraintSet.BOTTOM)
        cs.connect(p4.id, ConstraintSet.BOTTOM, hDivRight2.id, ConstraintSet.TOP)
        cs.connect(p5.id, ConstraintSet.TOP, hDivRight2.id, ConstraintSet.BOTTOM)
        cs.connect(p5.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        listOf(p2, p4, p5).forEach {
            cs.connect(it.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
            cs.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }
        textViews.forEach { cs.constrainPercentWidth(it.id, 0.5f) }
        listOf(p1, p3).forEach { cs.constrainPercentHeight(it.id, 0.5f) }
        listOf(p2, p4, p5).forEach { cs.constrainPercentHeight(it.id, 0.333f) }
    }

    private fun setup6PlayerConstraints(cs: ConstraintSet) {
        val vDivider = addDivider()
        val hDivider1 = addDivider()
        val hDivider2 = addDivider()
        cs.connect(vDivider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(vDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(vDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(vDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainWidth(vDivider.id, 1)

        cs.connect(hDivider1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(hDivider1.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(hDivider1.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(hDivider1.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainHeight(hDivider1.id, 1)
        cs.setVerticalBias(hDivider1.id, 0.333f)

        cs.connect(hDivider2.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(hDivider2.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(hDivider2.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(hDivider2.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.constrainHeight(hDivider2.id, 1)
        cs.setVerticalBias(hDivider2.id, 0.666f)

        val p1 = textViews[0]; val p2 = textViews[1]; val p3 = textViews[2]
        val p4 = textViews[3]; val p5 = textViews[4]; val p6 = textViews[5]

        listOf(p1, p3, p5).forEach {
            cs.connect(it.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            cs.connect(it.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        }
        listOf(p2, p4, p6).forEach {
            cs.connect(it.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
            cs.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }
        cs.connect(p1.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p1.id, ConstraintSet.BOTTOM, hDivider1.id, ConstraintSet.TOP)
        cs.connect(p2.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(p2.id, ConstraintSet.BOTTOM, hDivider1.id, ConstraintSet.TOP)
        cs.connect(p3.id, ConstraintSet.TOP, hDivider1.id, ConstraintSet.BOTTOM)
        cs.connect(p3.id, ConstraintSet.BOTTOM, hDivider2.id, ConstraintSet.TOP)
        cs.connect(p4.id, ConstraintSet.TOP, hDivider1.id, ConstraintSet.BOTTOM)
        cs.connect(p4.id, ConstraintSet.BOTTOM, hDivider2.id, ConstraintSet.TOP)
        cs.connect(p5.id, ConstraintSet.TOP, hDivider2.id, ConstraintSet.BOTTOM)
        cs.connect(p5.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(p6.id, ConstraintSet.TOP, hDivider2.id, ConstraintSet.BOTTOM)
        cs.connect(p6.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        textViews.forEach {
            cs.constrainPercentWidth(it.id, 0.5f)
            cs.constrainPercentHeight(it.id, 0.333f)
        }
    }
}