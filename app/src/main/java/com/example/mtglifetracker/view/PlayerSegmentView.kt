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
    private val longPressDelay = 800L // Increased from default

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.PlayerSegmentView,
            0, 0
        ).apply {
            try {
                angle = getInteger(R.styleable.PlayerSegmentView_angle, 0)
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

        // Set up internal listeners
        playerName.setOnClickListener { onPlayerNameClickListener?.invoke() }
        playerName.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    longPressRunnable = Runnable {
                        if (onUnloadProfileListener != null) {
                            onUnloadProfileListener?.invoke()
                        }
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, longPressDelay)
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
                    if (event.eventTime - event.downTime < longPressDelay) {
                        v.performClick()
                    }
                    true
                }
                else -> false
            }
        }
        commanderDamageSummary.setOnClickListener { onPlayerCountersClickListener?.invoke() }

        profilesRecyclerView.layoutManager = LinearLayoutManager(context)
        lifeCounter.addDismissibleOverlay(profilePopupContainer)
        lifeCounter.addDismissibleOverlay(playerCountersPopupContainer)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        commanderDamageSummary.rotation = -angle.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isRotated90degrees()) {
            super.onMeasure(heightMeasureSpec, widthMeasureSpec)
            setMeasuredDimension(measuredHeight, measuredWidth)
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    private fun isRotated90degrees(): Boolean {
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
        transformEvent(event)
        return super.dispatchTouchEvent(event)
    }

    fun updateUI(
        player: Player,
        allPlayers: List<Player>,
        allDamage: List<CommanderDamage>,
        isMassUpdate: Boolean
    ) {
        playerName.text = player.name

        val damageToThisPlayer = allDamage.filter {
            it.targetPlayerIndex == player.playerIndex && it.gameSize == allPlayers.size
        }
        if (allPlayers.isNotEmpty()) {
            commanderDamageSummary.updateView(player, allPlayers, damageToThisPlayer, this.angle)
        }

        if (isMassUpdate) {
            lifeCounter.setLifeAnimate(player.life)
        } else {
            lifeCounter.life = player.life
        }

        try {
            if (player.color != null) {
                val backgroundColor = player.color.toColorInt()
                this.setBackgroundColor(backgroundColor)
                val isDark = isColorDark(backgroundColor)
                val contrastColor = if (isDark) Color.WHITE else Color.BLACK
                playerName.setTextColor(contrastColor)
                lifeCounter.setTextColor(contrastColor)

            } else {
                this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
                playerName.setTextColor(Color.WHITE)
                lifeCounter.setTextColor(Color.WHITE)
            }
        } catch (_: Exception) {
            this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
            playerName.setTextColor(Color.WHITE)
            lifeCounter.setTextColor(Color.WHITE)
        }
    }

    fun setViewSizes(lifeSize: Float, nameSize: Float) {
        lifeCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, lifeSize)
        playerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameSize)
    }
}