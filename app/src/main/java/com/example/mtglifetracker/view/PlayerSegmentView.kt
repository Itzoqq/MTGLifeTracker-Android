package com.example.mtglifetracker.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.CommanderDamage
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.util.isColorDark
import com.google.android.material.card.MaterialCardView

@SuppressLint("ClickableViewAccessibility")
open class PlayerSegmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    val lifeCounter: LifeCounterView
    private val commanderDamageSummary: CommanderDamageSummaryView
    val playerName: TextView
    val profilePopupContainer: MaterialCardView
    val playerCountersPopupContainer: MaterialCardView
    val profilesRecyclerView: RecyclerView

    var onPlayerNameClickListener: (() -> Unit)? = null
    var onUnloadProfileListener: (() -> Unit)? = null
    var onPlayerCountersClickListener: (() -> Unit)? = null

    var angle: Int = 0
    private val longPressHandler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private val longPressDelay = 500L

    init {
        Logger.d("PlayerSegmentView init: Creating a new player segment.")

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlayerSegmentView,
            0, 0
        ).apply {
            try {
                angle = getInteger(R.styleable.PlayerSegmentView_angle, 0)
                Logger.d("PlayerSegmentView init: Angle from attributes is $angle.")
            } finally {
                recycle()
            }
        }
        rotation = 0f
        setWillNotDraw(false)

        inflate(context, R.layout.layout_player_segment, this)

        lifeCounter = findViewById(R.id.lifeCounter)
        commanderDamageSummary = findViewById(R.id.commander_damage_summary)
        playerName = findViewById(R.id.tv_player_name)
        profilePopupContainer = findViewById(R.id.profile_popup_container)
        playerCountersPopupContainer = findViewById(R.id.player_counters_popup_container)
        profilesRecyclerView = findViewById(R.id.profiles_recycler_view)

        playerName.setOnClickListener { onPlayerNameClickListener?.invoke() }
        playerName.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Logger.d("PlayerSegmentView: Player name ACTION_DOWN.")
                    longPressRunnable = Runnable {
                        Logger.i("PlayerSegmentView: Long press detected on player name. Invoking unload listener.")
                        onUnloadProfileListener?.invoke()
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, longPressDelay)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Logger.d("PlayerSegmentView: Player name ACTION_UP or ACTION_CANCEL.")
                    longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
                    if (event.eventTime - event.downTime < longPressDelay) {
                        Logger.d("PlayerSegmentView: Short press detected. Performing single click on player name.")
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
        commanderDamageSummary.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Logger.d("PlayerSegmentView: Commander summary ACTION_DOWN. Consuming event.")
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Logger.d("PlayerSegmentView: Commander summary ACTION_UP or ACTION_CANCEL. Performing click.")
                    v.performClick()
                    true
                }
                else -> false
            }
        }
        commanderDamageSummary.setOnClickListener {
            Logger.i("PlayerSegmentView: Commander summary clicked. Invoking counters listener.")
            onPlayerCountersClickListener?.invoke()
        }

        profilesRecyclerView.layoutManager = LinearLayoutManager(context)
        lifeCounter.addDismissibleOverlay(profilePopupContainer)
        lifeCounter.addDismissibleOverlay(playerCountersPopupContainer)

        Logger.d("PlayerSegmentView init: View tag is: ${this.tag}")
        if (this.tag != null) {
            commanderDamageSummary.tag = this.tag.toString() + "_commander_summary"
        } else {
            Logger.w("PlayerSegmentView init: this.tag is NULL. Cannot set summary tag yet.")
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Logger.d("PlayerSegmentView onAttachedToWindow. View tag is: ${this.tag}")
        if (this.tag != null) {
            commanderDamageSummary.tag = this.tag.toString() + "_commander_summary"
            Logger.d("PlayerSegmentView: Set commander summary tag to: ${commanderDamageSummary.tag}")
        } else {
            Logger.w("PlayerSegmentView onAttachedToWindow: this.tag is STILL NULL.")
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Logger.d("PlayerSegmentView onLayout: Setting rotation for commander damage summary to ${-angle.toFloat()}f.")
        commanderDamageSummary.rotation = -angle.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isRotated90or270degrees()) {
            Logger.d("PlayerSegmentView onMeasure (rotated): Swapping measure specs.")
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            Logger.d("PlayerSegmentView onMeasure (normal): Using standard measure specs.")
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun isRotated90or270degrees(): Boolean {
        return angle % 180 != 0
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.withSave {
            when (angle) {
                90 -> {
                    translate(width.toFloat(), 0f)
                    rotate(90f)
                }
                180 -> {
                    rotate(180f, width / 2f, height / 2f)
                }
                270, -90 -> {
                    translate(0f, height.toFloat())
                    rotate(270f)
                }
            }
            Logger.d("PlayerSegmentView dispatchDraw: Drawing children with rotation $angle.")
            super.dispatchDraw(this)
        }
    }

    fun transformEvent(event: MotionEvent) {
        val matrix = Matrix()
        when (angle) {
            90 -> {
                matrix.setRotate(-90f)
                matrix.postTranslate(0f, width.toFloat())
            }
            180 -> {
                matrix.setRotate(-180f)
                matrix.postTranslate(width.toFloat(), height.toFloat())
            }
            270, -90 -> {
                matrix.setRotate(-270f)
                matrix.postTranslate(height.toFloat(), 0f)
            }
        }
        event.transform(matrix)
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Logger.d("PlayerSegmentView dispatchTouchEvent (raw): x=${event.x}, y=${event.y}, angle=$angle")
        transformEvent(event)
        Logger.d("PlayerSegmentView dispatchTouchEvent (transformed): x=${event.x}, y=${event.y}")
        return super.dispatchTouchEvent(event)
    }

    fun updateUI(
        player: Player,
        allPlayers: List<Player>,
        allDamage: List<CommanderDamage>,
        isMassUpdate: Boolean
    ) {
        Logger.d("PlayerSegmentView updateUI for player ${player.playerIndex}: Name='${player.name}', Life=${player.life}, Color=${player.color}")
        playerName.text = player.name

        if (isMassUpdate) {
            lifeCounter.setLifeAnimate(player.life)
        } else {
            lifeCounter.life = player.life
        }

        try {
            val contrastColor: Int
            if (player.color != null) {
                val backgroundColor = player.color.toColorInt()
                this.setBackgroundColor(backgroundColor)
                // Check if the background color is dark to set a contrasting text color (white or black).
                contrastColor = if (isColorDark(backgroundColor)) Color.WHITE else Color.BLACK
                playerName.setTextColor(contrastColor)
                lifeCounter.setTextColor(contrastColor)
            } else {
                // If no color is set, use the default background and white text.
                this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
                contrastColor = Color.WHITE
                playerName.setTextColor(contrastColor)
                lifeCounter.setTextColor(contrastColor)
            }

            // Filter for commander damage entries relevant to this specific player and game size.
            val damageToThisPlayer = allDamage.filter {
                it.targetPlayerIndex == player.playerIndex && it.gameSize == allPlayers.size
            }
            // Update the commander damage summary view if there are players in the game.
            if (allPlayers.isNotEmpty()) {
                // Pass the calculated contrastColor to the summary view so it can adjust its own elements.
                commanderDamageSummary.updateView(player, allPlayers, damageToThisPlayer, this.angle, contrastColor)
            }

        } catch (e: Exception) {
            Logger.e(e, "PlayerSegmentView updateUI: Failed to parse color '${player.color}'. Reverting to defaults.")
            this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
            playerName.setTextColor(Color.WHITE)
            lifeCounter.setTextColor(Color.WHITE)
        }
    }

    fun setViewSizes(lifeSize: Float, nameSize: Float) {
        Logger.d("PlayerSegmentView setViewSizes: Setting lifeSize=$lifeSize, nameSize=$nameSize.")
        lifeCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, lifeSize)
        playerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameSize)
    }
}