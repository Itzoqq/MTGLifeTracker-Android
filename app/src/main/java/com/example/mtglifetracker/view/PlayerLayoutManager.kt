package com.example.mtglifetracker.view

import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.example.mtglifetracker.R

/**
 * Manages the creation and arrangement of player layouts within a ConstraintLayout.
 * ...
 */
class PlayerLayoutManager(
    private val container: ConstraintLayout,
    private val context: Context
) {

    val playerSegments = mutableListOf<RotatableLayout>()
    private val dividers = mutableListOf<View>()

    fun createPlayerLayouts(playerCount: Int) {
        val currentCount = playerSegments.size
        if (playerCount == currentCount) {
            return
        }

        dividers.forEach { container.removeView(it) }
        dividers.clear()

        while (playerSegments.size < playerCount) {
            val index = playerSegments.size
            val segment = RotatableLayout(context).apply {
                id = View.generateViewId()
                tag = "player_segment_$index"
            }
            container.addView(segment)
            playerSegments.add(segment)
        }
        while (playerSegments.size > playerCount) {
            val segmentToRemove = playerSegments.removeAt(playerSegments.lastIndex)
            container.removeView(segmentToRemove)
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(container)

        when (playerCount) {
            2 -> setupTwoPlayerLayout(constraintSet)
            3 -> setupThreePlayerLayout(constraintSet)
            4 -> setupFourPlayerLayout(constraintSet)
            5 -> setupFivePlayerLayout(constraintSet)
            6 -> setupSixPlayerLayout(constraintSet)
        }

        constraintSet.applyTo(container)

        // --- NEW AND IMPROVED SIZING LOGIC ---
        val resources = context.resources
        val largeSize = resources.getDimension(R.dimen.life_counter_text_size_large)
        val mediumSize = resources.getDimension(R.dimen.life_counter_text_size_medium)
        val smallSize = resources.getDimension(R.dimen.life_counter_text_size_small)

        playerSegments.forEachIndexed { index, segment ->
            val textSizeInPixels = when (playerCount) {
                2 -> largeSize
                3 -> if (index == 0) largeSize else mediumSize // Top segment is large, bottom two are medium
                4 -> mediumSize
                5 -> when (index) {
                    0, 1 -> mediumSize // Left two segments are medium
                    else -> smallSize   // Right three segments are small
                }
                6 -> mediumSize // Or smallSize, depending on preference for 6 players
                else -> mediumSize // Default case
            }
            segment.lifeCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels)
        }
        // --- END OF NEW LOGIC ---

        container.post {
            Log.d("LayoutManagerTest", "--- Layout applied for $playerCount players ---")
            playerSegments.forEachIndexed { index, segment ->
                val rect = android.graphics.Rect()
                segment.getGlobalVisibleRect(rect)
                Log.d("LayoutManagerTest",
                    "Segment $index (tag: ${segment.tag}): " +
                            "width=${segment.width}, height=${segment.height}, " +
                            "visibleRect=$rect"
                )
            }
        }
    }

    private fun setupTwoPlayerLayout(constraintSet: ConstraintSet) {
        val dividerId = View.generateViewId()
        addDivider(dividerId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL)

        // Player 1
        playerSegments[0].angle = 180
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, dividerId, ConstraintSet.TOP)

        // Player 2
        playerSegments[1].angle = 0
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, dividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        playerSegments.forEach {
            constraintSet.connect(it.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.constrainWidth(it.id, 0)
            constraintSet.constrainHeight(it.id, 0)
        }
    }

    private fun setupThreePlayerLayout(constraintSet: ConstraintSet) {
        val hDividerId = View.generateViewId()
        val vDividerId = View.generateViewId()
        addDivider(hDividerId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL)
        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL, hDividerId, ConstraintSet.PARENT_ID)


        // Player 1
        playerSegments[0].angle = 180
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDividerId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


        // Player 2
        playerSegments[1].angle = 90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 3
        playerSegments[2].angle = -90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        playerSegments.forEach {
            constraintSet.constrainWidth(it.id, 0)
            constraintSet.constrainHeight(it.id, 0)
        }
    }
    private fun setupFourPlayerLayout(constraintSet: ConstraintSet) {
        val hDividerId = View.generateViewId()
        val vDividerId = View.generateViewId()
        addDivider(hDividerId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL)
        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL)

        // Player 1
        playerSegments[0].angle = 90
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDividerId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 2
        playerSegments[1].angle = -90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, hDividerId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Player 3
        playerSegments[2].angle = 90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, hDividerId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 4
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
        val vDividerId = View.generateViewId()
        val hDividerLeftId = View.generateViewId()
        val hDividerRight1Id = View.generateViewId()
        val hDividerRight2Id = View.generateViewId()

        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL)
        addDivider(hDividerLeftId, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, ConstraintSet.PARENT_ID, vDividerId)
        addDivider(hDividerRight1Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, vDividerId, ConstraintSet.PARENT_ID, 0.33f)
        addDivider(hDividerRight2Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, vDividerId, ConstraintSet.PARENT_ID, 0.66f)

        // Player 1
        playerSegments[0].angle = 90
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDividerLeftId, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 2
        playerSegments[1].angle = 90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, hDividerLeftId, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 3
        playerSegments[2].angle = -90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, hDividerRight1Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Player 4
        playerSegments[3].angle = -90
        constraintSet.connect(playerSegments[3].id, ConstraintSet.TOP, hDividerRight1Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.BOTTOM, hDividerRight2Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Player 5
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
        val vDividerId = View.generateViewId()
        val hDivider1Id = View.generateViewId()
        val hDivider2Id = View.generateViewId()

        addDivider(vDividerId, constraintSet, ConstraintLayout.LayoutParams.VERTICAL)
        addDivider(hDivider1Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID, 0.33f)
        addDivider(hDivider2Id, constraintSet, ConstraintLayout.LayoutParams.HORIZONTAL, ConstraintSet.PARENT_ID, ConstraintSet.PARENT_ID, 0.66f)

        // Player 1
        playerSegments[0].angle = 90
        constraintSet.connect(playerSegments[0].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.BOTTOM, hDivider1Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[0].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 2
        playerSegments[1].angle = -90
        constraintSet.connect(playerSegments[1].id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.BOTTOM, hDivider1Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[1].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Player 3
        playerSegments[2].angle = 90
        constraintSet.connect(playerSegments[2].id, ConstraintSet.TOP, hDivider1Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.BOTTOM, hDivider2Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[2].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 4
        playerSegments[3].angle = -90
        constraintSet.connect(playerSegments[3].id, ConstraintSet.TOP, hDivider1Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.BOTTOM, hDivider2Id, ConstraintSet.TOP)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.START, vDividerId, ConstraintSet.END)
        constraintSet.connect(playerSegments[3].id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        // Player 5
        playerSegments[4].angle = 90
        constraintSet.connect(playerSegments[4].id, ConstraintSet.TOP, hDivider2Id, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraintSet.connect(playerSegments[4].id, ConstraintSet.END, vDividerId, ConstraintSet.START)

        // Player 6
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

        if (orientation == ConstraintLayout.LayoutParams.HORIZONTAL) {
            constraintSet.constrainHeight(viewId, 2)
            constraintSet.constrainWidth(viewId, 0)
            constraintSet.connect(viewId, ConstraintSet.START, startBarrier, if (startBarrier == ConstraintSet.PARENT_ID) ConstraintSet.START else ConstraintSet.END)
            constraintSet.connect(viewId, ConstraintSet.END, endBarrier, if (endBarrier == ConstraintSet.PARENT_ID) ConstraintSet.END else ConstraintSet.START)
            constraintSet.connect(viewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(viewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
            constraintSet.setVerticalBias(viewId, bias)
        } else { // VERTICAL
            constraintSet.constrainHeight(viewId, 0)
            constraintSet.constrainWidth(viewId, 2)
            constraintSet.connect(viewId, ConstraintSet.TOP, startBarrier, if (startBarrier == ConstraintSet.PARENT_ID) ConstraintSet.TOP else ConstraintSet.BOTTOM)
            constraintSet.connect(viewId, ConstraintSet.BOTTOM, endBarrier, if (endBarrier == ConstraintSet.PARENT_ID) ConstraintSet.BOTTOM else ConstraintSet.TOP)
            constraintSet.connect(viewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            constraintSet.connect(viewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constraintSet.setHorizontalBias(viewId, bias)
        }
    }
}