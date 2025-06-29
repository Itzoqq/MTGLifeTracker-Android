package com.example.mtglifetracker.view

import android.content.Context
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import com.example.mtglifetracker.R

class PlayerLayoutManager(
    private val container: ConstraintLayout,
    private val context: Context
) {

    // The list now holds PlayerSegmentView directly
    val playerSegments = mutableListOf<PlayerSegmentView>()
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
            // Create a PlayerSegmentView instead of a RotatableLayout
            val segment = PlayerSegmentView(context).apply {
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

        val resources = context.resources
        val lifeLarge = resources.getDimension(R.dimen.life_counter_text_size_large)
        val lifeMedium = resources.getDimension(R.dimen.life_counter_text_size_medium)
        val lifeSmall = resources.getDimension(R.dimen.life_counter_text_size_small)
        val nameLarge = resources.getDimension(R.dimen.nickname_text_size_large)
        val nameMedium = resources.getDimension(R.dimen.nickname_text_size_medium)
        val nameSmall = resources.getDimension(R.dimen.nickname_text_size_small)
        val countersWidthLarge = resources.getDimensionPixelSize(R.dimen.player_counters_width_large)
        val countersHeightLarge = resources.getDimensionPixelSize(R.dimen.player_counters_height_large)
        val countersWidthMedium = resources.getDimensionPixelSize(R.dimen.player_counters_width_medium)
        val countersHeightMedium = resources.getDimensionPixelSize(R.dimen.player_counters_height_medium)
        val countersWidthSmall = resources.getDimensionPixelSize(R.dimen.player_counters_width_small)
        val countersHeightSmall = resources.getDimensionPixelSize(R.dimen.player_counters_height_small)

        playerSegments.forEachIndexed { index, segment ->
            val (lifeSize, nameSize) = when (playerCount) {
                2 -> lifeLarge to nameLarge
                3 -> if (index == 0) lifeLarge to nameLarge else lifeMedium to nameMedium
                4 -> lifeMedium to nameMedium
                5 -> if (index < 2) lifeMedium to nameMedium else lifeSmall to nameSmall
                6 -> lifeMedium to nameMedium
                else -> lifeMedium to nameMedium
            }

            val (countersWidth, countersHeight) = when (playerCount) {
                2 -> countersWidthLarge to countersHeightLarge
                3 -> if (index == 0) countersWidthLarge to countersHeightLarge else countersWidthMedium to countersHeightMedium
                4 -> countersWidthMedium to countersHeightMedium
                5 -> if (index < 2) countersWidthMedium to countersHeightMedium else countersWidthSmall to countersHeightSmall
                6 -> countersWidthSmall to countersHeightSmall
                else -> countersWidthMedium to countersHeightMedium
            }

            // Call the sizing method directly on the segment
            segment.setViewSizes(lifeSize, nameSize, countersWidth, countersHeight)
        }

        container.post {
            Log.d("LayoutManagerTest", "--- Layout applied for $playerCount players ---")
            playerSegments.forEachIndexed { index, segment ->
                val rect = android.graphics.Rect()
                segment.getGlobalVisibleRect(rect)
                Log.d(
                    "LayoutManagerTest",
                    "Segment $index (tag: ${segment.tag}): " +
                            "width=${segment.width}, height=${segment.height}, " +
                            "visibleRect=$rect"
                )
            }
        }
    }

    // The rest of the setup...Layout and addDivider methods remain the same,
    // as they operate on the `playerSegments` list which is now of the correct type.
    // ...
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