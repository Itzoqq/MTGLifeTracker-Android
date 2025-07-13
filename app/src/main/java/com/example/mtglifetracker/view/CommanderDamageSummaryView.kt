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
import com.example.mtglifetracker.util.isColorDark

class CommanderDamageSummaryView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var textViews = mutableListOf<TextView>()
    private var dividers = mutableListOf<View>()
    private var currentPlayerCount = 0
    private var playerGrid = listOf<Player>()
    private val backgroundPaint = Paint()

    fun updateView(
        currentPlayer: Player,
        allPlayersInGame: List<Player>,
        damageToCurrentPlayer: List<CommanderDamage>,
        angle: Int
    ) {
        val playerCount = allPlayersInGame.size
        if (playerCount != currentPlayerCount) {
            setupLayout(playerCount)
        }

        playerGrid = getPlayerGrid(playerCount, allPlayersInGame)
        val damageMap = damageToCurrentPlayer.associateBy { it.sourcePlayerIndex }

        textViews.forEachIndexed { index, textView ->
            val sourcePlayer = playerGrid.getOrNull(index)
            if (sourcePlayer == null) {
                textView.visibility = GONE
                return@forEachIndexed
            }

            textView.visibility = VISIBLE
            textView.rotation = angle.toFloat()
            textView.background = null // Remove direct background

            val textColor = if (sourcePlayer.color != null && isColorDark(sourcePlayer.color.toColorInt())) {
                Color.WHITE
            } else {
                Color.BLACK
            }
            textView.setTextColor(textColor)

            if (sourcePlayer.playerIndex == currentPlayer.playerIndex) {
                textView.text = context.getString(R.string.me)
                textView.setTextColor(ContextCompat.getColor(context, R.color.purple_200))
            } else {
                textView.text = (damageMap[sourcePlayer.playerIndex]?.damage ?: 0).toString()
            }
        }
        invalidate() // Trigger a redraw
    }

    override fun onDraw(canvas: Canvas) {
        // Draw custom backgrounds first
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
                } catch (_: Exception) {
                    // Color parsing failed, do not draw background
                }
            }
        }
        // Draw text and dividers on top
        super.onDraw(canvas)
    }


    private fun getPlayerGrid(playerCount: Int, allPlayers: List<Player>): List<Player> {
        if (allPlayers.isEmpty()) return emptyList()
        val sortedPlayers = allPlayers.sortedBy { it.playerIndex }
        return when (playerCount) {
            5 -> listOf(sortedPlayers[0], sortedPlayers[2], sortedPlayers[1], sortedPlayers[3], sortedPlayers[4])
            else -> sortedPlayers
        }
    }

    private fun setupLayout(playerCount: Int) {
        removeAllViews()
        textViews.clear()
        dividers.clear()
        if (playerCount < 2) return

        (0 until playerCount).forEach { i ->
            textViews.add(createCounterTextView())
        }

        val constraintSet = ConstraintSet()
        constraintSet.clone(this)
        applyConstraints(constraintSet, playerCount)
        constraintSet.applyTo(this)
        currentPlayerCount = playerCount
    }

    private fun applyConstraints(cs: ConstraintSet, playerCount: Int) {
        when (playerCount) {
            2 -> setup2PlayerConstraints(cs)
            3 -> setup3PlayerConstraints(cs)
            4 -> setup4PlayerConstraints(cs)
            5 -> setup5PlayerConstraints(cs)
            6 -> setup6PlayerConstraints(cs)
        }
    }

    private fun createCounterTextView(): TextView {
        return TextView(context).apply {
            id = generateViewId()
            layoutParams = LayoutParams(0, 0)
            setTextColor(ContextCompat.getColor(context, R.color.white))
            gravity = Gravity.CENTER
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            addView(this)
        }
    }

    private fun addDivider(): View {
        return View(context).apply {
            id = generateViewId()
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            dividers.add(this)
            addView(this)
        }
    }

    private fun centerDivider(cs: ConstraintSet, viewId: Int, orientation: Int) {
        cs.connect(viewId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        cs.connect(viewId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        cs.connect(viewId, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(viewId, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        if (orientation == LayoutParams.HORIZONTAL) {
            cs.constrainHeight(viewId, 1)
            cs.constrainWidth(viewId, 0)
        } else {
            cs.constrainWidth(viewId, 1)
            cs.constrainHeight(viewId, 0)
        }
    }

    private fun setup2PlayerConstraints(cs: ConstraintSet) {
        val hDivider = addDivider()
        centerDivider(cs, hDivider.id, LayoutParams.HORIZONTAL)
        val p1 = textViews[0]
        val p2 = textViews[1]
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
        centerDivider(cs, hDivider.id, LayoutParams.HORIZONTAL)
        centerDivider(cs, vDivider.id, LayoutParams.VERTICAL)
        val p1 = textViews[0]
        val p2 = textViews[1]
        val p3 = textViews[2]
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
        cs.constrainPercentWidth(textViews[0].id, 1.0f)
        cs.constrainPercentHeight(textViews[0].id, 0.5f)
        cs.constrainPercentWidth(textViews[1].id, 0.5f)
        cs.constrainPercentHeight(textViews[1].id, 0.5f)
        cs.constrainPercentWidth(textViews[2].id, 0.5f)
        cs.constrainPercentHeight(textViews[2].id, 0.5f)
    }

    private fun setup4PlayerConstraints(cs: ConstraintSet) {
        val hDivider = addDivider()
        val vDivider = addDivider()
        centerDivider(cs, hDivider.id, LayoutParams.HORIZONTAL)
        centerDivider(cs, vDivider.id, LayoutParams.VERTICAL)
        val p1 = textViews[0]
        val p2 = textViews[1]
        val p3 = textViews[2]
        val p4 = textViews[3]
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
        centerDivider(cs, vDivider.id, LayoutParams.VERTICAL)
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

        val p1 = textViews[0]
        val p2 = textViews[1]
        val p3 = textViews[2]
        val p4 = textViews[3]
        val p5 = textViews[4]

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
        centerDivider(cs, vDivider.id, LayoutParams.VERTICAL)
        centerDivider(cs, hDivider1.id, LayoutParams.HORIZONTAL); cs.setVerticalBias(hDivider1.id, 0.333f)
        centerDivider(cs, hDivider2.id, LayoutParams.HORIZONTAL); cs.setVerticalBias(hDivider2.id, 0.666f)

        val topLeft = textViews[0]
        val topRight = textViews[1]
        val midLeft = textViews[2]
        val midRight = textViews[3]
        val bottomLeft = textViews[4]
        val bottomRight = textViews[5]

        listOf(topLeft, midLeft, bottomLeft).forEach {
            cs.connect(it.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            cs.connect(it.id, ConstraintSet.END, vDivider.id, ConstraintSet.START)
        }
        listOf(topRight, midRight, bottomRight).forEach {
            cs.connect(it.id, ConstraintSet.START, vDivider.id, ConstraintSet.END)
            cs.connect(it.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        }
        cs.connect(topLeft.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(topLeft.id, ConstraintSet.BOTTOM, hDivider1.id, ConstraintSet.TOP)
        cs.connect(topRight.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        cs.connect(topRight.id, ConstraintSet.BOTTOM, hDivider1.id, ConstraintSet.TOP)
        cs.connect(midLeft.id, ConstraintSet.TOP, hDivider1.id, ConstraintSet.BOTTOM)
        cs.connect(midLeft.id, ConstraintSet.BOTTOM, hDivider2.id, ConstraintSet.TOP)
        cs.connect(midRight.id, ConstraintSet.TOP, hDivider1.id, ConstraintSet.BOTTOM)
        cs.connect(midRight.id, ConstraintSet.BOTTOM, hDivider2.id, ConstraintSet.TOP)
        cs.connect(bottomLeft.id, ConstraintSet.TOP, hDivider2.id, ConstraintSet.BOTTOM)
        cs.connect(bottomLeft.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        cs.connect(bottomRight.id, ConstraintSet.TOP, hDivider2.id, ConstraintSet.BOTTOM)
        cs.connect(bottomRight.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        textViews.forEach {
            cs.constrainPercentWidth(it.id, 0.5f)
            cs.constrainPercentHeight(it.id, 0.333f)
        }
    }
}