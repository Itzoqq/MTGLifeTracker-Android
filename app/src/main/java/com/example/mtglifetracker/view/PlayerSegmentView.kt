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

/**
 * A custom [ConstraintLayout] that represents a single player's area on the screen.
 *
 * This is a highly complex view responsible for:
 * - Displaying a player's life total via a [LifeCounterView].
 * - Displaying a player's name and commander damage summary.
 * - Handling arbitrary rotation (0, 90, 180, -90 degrees) to orient the UI correctly for each player.
 * - Transforming touch events to correctly interact with child views when rotated.
 * - Managing the visibility of popups for profile selection and other counters.
 */
@SuppressLint("ClickableViewAccessibility")
open class PlayerSegmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // --- View Properties ---
    val lifeCounter: LifeCounterView
    private val commanderDamageSummary: CommanderDamageSummaryView
    val playerName: TextView
    val profilePopupContainer: MaterialCardView
    val playerCountersPopupContainer: MaterialCardView
    val profilesRecyclerView: RecyclerView

    // --- Listeners and Handlers ---
    var onPlayerNameClickListener: (() -> Unit)? = null
    var onUnloadProfileListener: (() -> Unit)? = null
    var onPlayerCountersClickListener: (() -> Unit)? = null

    // --- State Properties ---
    var angle: Int = 0
    private val longPressHandler = Handler(Looper.getMainLooper())
    private var longPressRunnable: Runnable? = null
    private val longPressDelay = 500L // 500ms for a long press

    init {
        // --- Initialization Block ---
        Logger.d("PlayerSegmentView init: Creating a new player segment.")

        // Read the 'angle' attribute from the XML layout, if provided.
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
        rotation = 0f // The view itself doesn't rotate; we rotate its canvas during drawing.
        setWillNotDraw(false) // Required for a layout to have its onDraw method called.

        // Inflate the child views from XML.
        inflate(context, R.layout.layout_player_segment, this)

        // Cache references to child views.
        lifeCounter = findViewById(R.id.lifeCounter)
        commanderDamageSummary = findViewById(R.id.commander_damage_summary)
        playerName = findViewById(R.id.tv_player_name)
        profilePopupContainer = findViewById(R.id.profile_popup_container)
        playerCountersPopupContainer = findViewById(R.id.player_counters_popup_container)
        profilesRecyclerView = findViewById(R.id.profiles_recycler_view)

        // Set up a complex touch listener for the player name to handle both single-tap and long-press.
        playerName.setOnClickListener { onPlayerNameClickListener?.invoke() }
        playerName.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Logger.d("PlayerSegmentView: Player name ACTION_DOWN.")
                    // On press, schedule a long-press action to run after a delay.
                    longPressRunnable = Runnable {
                        Logger.i("PlayerSegmentView: Long press detected on player name. Invoking unload listener.")
                        onUnloadProfileListener?.invoke()
                    }
                    longPressHandler.postDelayed(longPressRunnable!!, longPressDelay)
                    true // Consume the event.
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Logger.d("PlayerSegmentView: Player name ACTION_UP or ACTION_CANCEL.")
                    // On release, cancel the pending long-press action.
                    longPressRunnable?.let { longPressHandler.removeCallbacks(it) }
                    // If the press duration was shorter than the long-press delay, it was a single tap.
                    if (event.eventTime - event.downTime < longPressDelay) {
                        Logger.d("PlayerSegmentView: Short press detected. Performing single click on player name.")
                        v.performClick()
                    }
                    true // Consume the event.
                }
                else -> false
            }
        }
        // This touch listener intercepts touch events on the commander damage summary.
        // Returning true for ACTION_DOWN prevents the touch from propagating to the parent LifeCounterView,
        // which would incorrectly trigger a life change.
        commanderDamageSummary.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Logger.d("PlayerSegmentView: Commander summary ACTION_DOWN. Consuming event.")
                    true // Crucial: Consume the event here to prevent it from reaching the parent.
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    Logger.d("PlayerSegmentView: Commander summary ACTION_UP or ACTION_CANCEL. Performing click.")
                    v.performClick() // Trigger the standard click action on release.
                    true
                }
                else -> false
            }
        }
        // The standard click listener still defines what happens when performClick() is called.
        commanderDamageSummary.setOnClickListener {
            Logger.i("PlayerSegmentView: Commander summary clicked. Invoking counters listener.")
            onPlayerCountersClickListener?.invoke()
        }

        // --- RecyclerView and Popup Setup ---
        profilesRecyclerView.layoutManager = LinearLayoutManager(context)
        // Register popups with the LifeCounterView so it can dismiss them on tap.
        lifeCounter.addDismissibleOverlay(profilePopupContainer)
        lifeCounter.addDismissibleOverlay(playerCountersPopupContainer)

        Logger.d("PlayerSegmentView init: View tag is: ${this.tag}")
        if (this.tag != null) {
            commanderDamageSummary.tag = this.tag.toString() + "_commander_summary"
        } else {
            Logger.w("PlayerSegmentView init: this.tag is NULL. Cannot set summary tag yet.")
        }
    }

    /**
     * Called when the view is attached to a window. This is a reliable place to ensure
     * that tags have been set by the [PlayerLayoutManager].
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Logger.d("PlayerSegmentView onAttachedToWindow. View tag is: ${this.tag}")
        if (this.tag != null) {
            // Ensure the child summary view also has a unique tag for testing.
            commanderDamageSummary.tag = this.tag.toString() + "_commander_summary"
            Logger.d("PlayerSegmentView: Set commander summary tag to: ${commanderDamageSummary.tag}")
        } else {
            Logger.w("PlayerSegmentView onAttachedToWindow: this.tag is STILL NULL.")
        }
    }

    /**
     * Called when the view assigns a size and position to its children.
     * We use this to ensure the child `CommanderDamageSummaryView` is correctly counter-rotated.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Logger.d("PlayerSegmentView onLayout: Setting rotation for commander damage summary to ${-angle.toFloat()}f.")
        // The summary view inside the player segment must be counter-rotated to appear upright.
        commanderDamageSummary.rotation = -angle.toFloat()
    }

    /**
     * Measures the view and its content to determine the measured width and height.
     * For 90 or 270-degree rotations, we swap the measure specs to ensure children
     * are measured correctly within the rotated space.
     */
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

    /**
     * Draws the view's content. We override this to apply a rotation to the canvas
     * before any of the child views are drawn.
     */
    override fun dispatchDraw(canvas: Canvas) {
        canvas.withSave {
            // Apply the appropriate rotation transform to the canvas.
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
            // After transforming the canvas, let the superclass draw the children onto it.
            super.dispatchDraw(this)
        }
    }

    /**
     * Transforms touch event coordinates from the parent's coordinate system to this
     * view's local (rotated) coordinate system. This is crucial for correct touch
     * handling in rotated views.
     */
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

    /**
     * Dispatches touch events to child views. We override this to apply our coordinate
     * transformation before the event is passed down the view hierarchy.
     */
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Logger.d("PlayerSegmentView dispatchTouchEvent (raw): x=${event.x}, y=${event.y}, angle=$angle")
        transformEvent(event)
        Logger.d("PlayerSegmentView dispatchTouchEvent (transformed): x=${event.x}, y=${event.y}")
        return super.dispatchTouchEvent(event)
    }

    /**
     * Updates all UI elements within this segment to reflect the given player state.
     */
    fun updateUI(
        player: Player,
        allPlayers: List<Player>,
        allDamage: List<CommanderDamage>,
        isMassUpdate: Boolean
    ) {
        Logger.d("PlayerSegmentView updateUI for player ${player.playerIndex}: Name='${player.name}', Life=${player.life}, Color=${player.color}")
        playerName.text = player.name

        // Filter the full damage list to only what's relevant for this player.
        val damageToThisPlayer = allDamage.filter {
            it.targetPlayerIndex == player.playerIndex && it.gameSize == allPlayers.size
        }
        if (allPlayers.isNotEmpty()) {
            commanderDamageSummary.updateView(player, allPlayers, damageToThisPlayer, this.angle)
        }

        // Use a smoother animation for game resets.
        if (isMassUpdate) {
            lifeCounter.setLifeAnimate(player.life)
        } else {
            lifeCounter.life = player.life
        }

        // Set background and text colors based on the assigned profile color.
        try {
            if (player.color != null) {
                val backgroundColor = player.color.toColorInt()
                this.setBackgroundColor(backgroundColor)
                // Determine if the text should be black or white for contrast.
                val contrastColor = if (isColorDark(backgroundColor)) Color.WHITE else Color.BLACK
                playerName.setTextColor(contrastColor)
                lifeCounter.setTextColor(contrastColor)
            } else {
                // Revert to default colors if no profile is assigned.
                this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
                playerName.setTextColor(Color.WHITE)
                lifeCounter.setTextColor(Color.WHITE)
            }
        } catch (e: Exception) {
            Logger.e(e, "PlayerSegmentView updateUI: Failed to parse color '${player.color}'. Reverting to defaults.")
            this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
            playerName.setTextColor(Color.WHITE)
            lifeCounter.setTextColor(Color.WHITE)
        }
    }

    /**
     * Sets the text sizes for the life counter and player name.
     * This is called by the [PlayerLayoutManager] to adjust font sizes based on the
     * number of players on screen.
     */
    fun setViewSizes(lifeSize: Float, nameSize: Float) {
        Logger.d("PlayerSegmentView setViewSizes: Setting lifeSize=$lifeSize, nameSize=$nameSize.")
        lifeCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, lifeSize)
        playerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameSize)
    }
}