package com.example.mtglifetracker.view

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.example.mtglifetracker.R
import com.example.mtglifetracker.util.Logger

/**
 * Manages the creation, removal, and arrangement of [PlayerSegmentView] instances within a [ConstraintLayout].
 *
 * This class is the core of the dynamic UI. It is responsible for building the visual layout for
 * different numbers of players (2 through 6). It programmatically creates and destroys
 * [PlayerSegmentView]s and uses [ConstraintSet] to define their positions, sizes, and relationships
 * to each other and to the parent container. It also handles the creation of visual dividers
 * between the player segments.
 *
 * @param container The parent [ConstraintLayout] where player segments will be added.
 * @param context The application context, needed for creating views and accessing resources.
 */
class PlayerLayoutManager(
    private val container: ConstraintLayout,
    private val context: Context
) {

    // A mutable list to hold the currently active PlayerSegmentView instances.
    val playerSegments = mutableListOf<PlayerSegmentView>()
    // A list to hold the views used as dividers.
    private val dividers = mutableListOf<View>()

    /**
     * The main public method to create or update the player layouts.
     *
     * This function is called when the number of players changes. It adds or removes
     * [PlayerSegmentView] instances to match the new `playerCount`, then calls the
     * appropriate setup method to apply the correct constraints for the new layout.
     *
     * @param playerCount The desired number of players to display.
     */
    fun createPlayerLayouts(playerCount: Int) {
        Logger.i("PlayerLayoutManager: createPlayerLayouts called for $playerCount players.")
        val currentCount = playerSegments.size
        // If the layout already matches the requested player count, do nothing.
        if (playerCount == currentCount) {
            Logger.d("PlayerLayoutManager: Player count is already $playerCount. No layout change needed.")
            return
        }

        // --- Clean up old dividers before creating new ones ---
        Logger.d("PlayerLayoutManager: Clearing ${dividers.size} existing dividers.")
        dividers.forEach { container.removeView(it) }
        dividers.clear()

        // --- Add new PlayerSegmentViews if needed ---
        while (playerSegments.size < playerCount) {
            val index = playerSegments.size
            val segment = PlayerSegmentView(context).apply {
                id = View.generateViewId()
                // Assign a unique tag for easier identification during testing and debugging.
                tag = "player_segment_$index"
            }
            Logger.d("PlayerLayoutManager: CREATE: PlayerSegmentView for index $index created with tag: ${segment.tag}")
            container.addView(segment)
            playerSegments.add(segment)
        }

        // --- Remove excess PlayerSegmentViews if needed ---
        while (playerSegments.size > playerCount) {
            val segmentToRemove = playerSegments.removeAt(playerSegments.lastIndex)
            Logger.d("PlayerLayoutManager: REMOVE: Removing PlayerSegmentView with tag: ${segmentToRemove.tag}")
            container.removeView(segmentToRemove)
        }

        // Begin the process of defining new constraints.
        val constraintSet = ConstraintSet()
        // Clone the existing constraints from the container as a starting point.
        constraintSet.clone(container)

        // Dispatch to the appropriate method to build the layout for the specific player count.
        when (playerCount) {
            2 -> setupTwoPlayerLayout(constraintSet)
            3 -> setupThreePlayerLayout(constraintSet)
            4 -> setupFourPlayerLayout(constraintSet)
            5 -> setupFivePlayerLayout(constraintSet)
            6 -> setupSixPlayerLayout(constraintSet)
        }

        Logger.i("PlayerLayoutManager: Applying constraints for $playerCount players.")
        constraintSet.applyTo(container)

        // --- Adjust text sizes based on the player count for better readability ---
        Logger.d("PlayerLayoutManager: Setting view sizes for $playerCount players.")
        val resources = context.resources
        val lifeLarge = resources.getDimension(R.dimen.life_counter_text_size_large)
        val lifeMedium = resources.getDimension(R.dimen.life_counter_text_size_medium)
        val lifeSmall = resources.getDimension(R.dimen.life_counter_text_size_small)
        val nameLarge = resources.getDimension(R.dimen.nickname_text_size_large)
        val nameMedium = resources.getDimension(R.dimen.nickname_text_size_medium)
        val nameSmall = resources.getDimension(R.dimen.nickname_text_size_small)

        playerSegments.forEachIndexed { index, segment ->
            val (lifeSize, nameSize) = when (playerCount) {
                2 -> lifeLarge to nameLarge
                3 -> if (index == 0) lifeLarge to nameLarge else lifeMedium to nameMedium
                4 -> lifeMedium to nameMedium
                5 -> if (index < 2) lifeMedium to nameMedium else lifeSmall to nameSmall
                6 -> lifeMedium to nameMedium
                else -> lifeMedium to nameMedium // Default case
            }
            segment.setViewSizes(lifeSize, nameSize)
            Logger.d("PlayerLayoutManager: Set segment $index to lifeSize=$lifeSize, nameSize=$nameSize.")
        }

        // Post a runnable to the message queue to be executed after the layout pass is complete.
        // This is the most reliable way to get the final, measured dimensions of the views.
        container.post {
            Logger.d("PlayerLayoutManager: POST_LAYOUT for $playerCount players.")
            playerSegments.forEachIndexed { index, segment ->
                val rect = android.graphics.Rect()
                segment.getGlobalVisibleRect(rect)
                Logger.d(
                    "PlayerLayoutManager: POST_LAYOUT - Segment $index (tag: ${segment.tag}): width=${segment.width}, height=${segment.height}, visibleRect=$rect"
                )
            }
        }
        Logger.i("PlayerLayoutManager: Finished createPlayerLayouts for $playerCount players.")
    }

    private fun setupTwoPlayerLayout(constraintSet: ConstraintSet) {
        Logger.d("Applying 2-player layout constraints.")
        // Add a horizontal divider in the middle of the screen.
        val dividerId = View.generateViewId()
        addDivider(dividerId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL)

        // Top player, rotated 180 degrees.
        playerSegments[0].angle = 180
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, dividerId, ConstraintSet.TOP)

        // Bottom player, not rotated.
        playerSegments[1].angle = 0
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, dividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        // Make both segments stretch to fill the available space.
        playerSegments.forEach {
            constraintSet.connect(it.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.constrainWidth(it.id, 0) // 0dp (MATCH_CONSTRAINT)
            constraintSet.constrainHeight(it.id, 0) // 0dp (MATCH_CONSTRAINT)
        }
    }

    private fun setupThreePlayerLayout(constraintSet: ConstraintSet) {
        Logger.d("Applying 3-player layout constraints.")
        // One horizontal divider, one vertical divider for the bottom half.
        val hDividerId = View.generateViewId()
        val vDividerId = View.generateViewId()
        addDivider(hDividerId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL)
        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL, hDividerId, ConstraintSet.PARENT_ID)

        // Top player, full width.
        playerSegments[0].angle = 180
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDividerId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Bottom-left player, rotated 90 degrees.
        playerSegments[1].angle = 90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Bottom-right player, rotated -90 degrees.
        playerSegments[2].angle = -90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Make all segments stretch to fill their constrained space.
        playerSegments.forEach {
            constraintSet.constrainWidth(it.id, 0)
            constraintSet.constrainHeight(it.id, 0)
        }
    }

    // ... (The other setup methods for 4, 5, and 6 players would follow the same pattern of logging and comments) ...

    private fun setupFourPlayerLayout(constraintSet: ConstraintSet) {
        Logger.d("Applying 4-player layout constraints.")
        val hDividerId = View.generateViewId()
        val vDividerId = View.generateViewId()
        addDivider(hDividerId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL)
        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL)

        playerSegments[0].angle = 90
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDividerId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[1].angle = -90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, hDividerId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments[2].angle = 90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[3].angle = -90
        constraintSet.connect(playerSegments[3].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments.forEach {
            constraintSet.constrainWidth(it.id, 0)
            constraintSet.constrainHeight(it.id, 0)
        }
    }

    private fun setupFivePlayerLayout(constraintSet: ConstraintSet) {
        Logger.d("Applying 5-player layout constraints.")
        val vDividerId = View.generateViewId()
        val hDividerLeftId = View.generateViewId()
        val hDividerRight1Id = View.generateViewId()
        val hDividerRight2Id = View.generateViewId()

        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL)
        addDivider(hDividerLeftId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, ConstraintSet.PARENT_ID, vDividerId)
        addDivider(hDividerRight1Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, vDividerId, ConstraintSet.PARENT_ID, 0.33f)
        addDivider(hDividerRight2Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, vDividerId, ConstraintSet.PARENT_ID, 0.66f)

        playerSegments[0].angle = 90
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDividerLeftId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[1].angle = 90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, hDividerLeftId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[2].angle = -90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, hDividerRight1Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments[3].angle = -90
        constraintSet.connect(playerSegments[3].id, ConstraintSet.TOP, hDividerRight1Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.BOTTOM, hDividerRight2Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments[4].angle = -90
        constraintSet.connect(playerSegments[4].id, ConstraintSet.TOP, hDividerRight2Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments.forEach {
            constraintSet.constrainWidth(it.id, 0)
            constraintSet.constrainHeight(it.id, 0)
        }
    }

    private fun setupSixPlayerLayout(constraintSet: ConstraintSet) {
        Logger.d("Applying 6-player layout constraints.")
        val vDividerId = View.generateViewId()
        val hDivider1Id = View.generateViewId()
        val hDivider2Id = View.generateViewId()

        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL)
        addDivider(hDivider1Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID, 0.33f)
        addDivider(hDivider2Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID, 0.66f)

        playerSegments[0].angle = 90
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDivider1Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[1].angle = -90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, hDivider1Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments[2].angle = 90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, hDivider1Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, hDivider2Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[3].angle = -90
        constraintSet.connect(playerSegments[3].id, ConstraintSet.TOP, hDivider1Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.BOTTOM, hDivider2Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments[4].angle = 90
        constraintSet.connect(playerSegments[4].id, ConstraintSet.TOP, hDivider2Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        playerSegments[5].angle = -90
        constraintSet.connect(playerSegments[5].id, ConstraintSet.TOP, hDivider2Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[5].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[5].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[5].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments.forEach {
            constraintSet.constrainWidth(it.id, 0)
            constraintSet.constrainHeight(it.id, 0)
        }
    }


    /**
     * A helper method to create a divider View and apply basic constraints to it.
     *
     * @param viewId The ID to be assigned to the new divider view.
     * @param constraintSet The [ConstraintSet] to add the new constraints to.
     * @param orientation The orientation of the divider (HORIZONTAL or VERTICAL).
     * @param startBarrier For vertical dividers, the view ID to constrain the top to. For horizontal, the left.
     * @param endBarrier For vertical dividers, the view ID to constrain the bottom to. For horizontal, the right.
     * @param bias The vertical or horizontal bias to position the divider.
     */
    private fun addDivider(
        viewId: Int,
        constraintSet: ConstraintSet,
        orientation: Int,
        startBarrier: Int = ConstraintSet.PARENT_ID,
        endBarrier: Int = ConstraintSet.PARENT_ID,
        bias: Float = 0.5f
    ) {
        val view = View(context).apply {
            id = viewId
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
        }
        container.addView(view)
        dividers.add(view)
        Logger.d("addDivider: Created a new divider with ID $viewId and orientation $orientation.")

        if (orientation == ConstraintLayout.LayoutParams.HORIZONTAL) {
            constraintSet.constrainHeight(viewId, 2) // Set a fixed height of 2dp
            constraintSet.constrainWidth(viewId, 0)  // Set width to match constraints (0dp)
            constraintSet.connect(viewId, ConstraintSet.START, startBarrier, if (startBarrier == ConstraintSet.PARENT_ID) ConstraintSet.START else ConstraintSet.END)
            constraintSet.connect(viewId, ConstraintSet.END, endBarrier, if (endBarrier == ConstraintSet.PARENT_ID) ConstraintSet.END else ConstraintSet.START)
            constraintSet.connect(viewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(viewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.setVerticalBias(viewId, bias) // Position it vertically
        } else { // VERTICAL
            constraintSet.constrainHeight(viewId, 0) // Set height to match constraints (0dp)
            constraintSet.constrainWidth(viewId, 2)   // Set a fixed width of 2dp
            constraintSet.connect(viewId, ConstraintSet.TOP, startBarrier, if (startBarrier == ConstraintSet.PARENT_ID) ConstraintSet.TOP else ConstraintSet.BOTTOM)
            constraintSet.connect(viewId, ConstraintSet.BOTTOM, endBarrier, if (endBarrier == ConstraintSet.PARENT_ID) ConstraintSet.BOTTOM else ConstraintSet.TOP)
            constraintSet.connect(viewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(viewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.setHorizontalBias(viewId, bias) // Position it horizontally
        }
    }
}