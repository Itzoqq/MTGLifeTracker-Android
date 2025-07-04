package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.util.isColorDark
import com.google.android.material.card.MaterialCardView

/**
 * A custom view that represents the UI for a single player segment.
 * It handles its own UI, rotation, and touch event transformation.
 */
open class PlayerSegmentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    // UI View References
    val lifeCounter: LifeCounterView
    val playerCounters: PlayerCountersView
    val playerName: TextView
    val profilePopupContainer: MaterialCardView
    val playerCountersPopupContainer: MaterialCardView
    val profilesRecyclerView: RecyclerView
    val unloadProfileButton: ImageView

    // Public Listeners
    var onPlayerNameClickListener: (() -> Unit)? = null
    var onUnloadProfileListener: (() -> Unit)? = null
    var onPlayerCountersClickListener: (() -> Unit)? = null

    // Rotation Property
    var angle: Int = 0

    init {
        // This now correctly references the styleable from attrs.xml
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
        rotation = 0f // We handle rotation manually in dispatchDraw
        setWillNotDraw(false)

        // Inflate the segment's layout into this view
        inflate(context, R.layout.layout_player_segment, this)

        // Find all child views
        lifeCounter = findViewById(R.id.lifeCounter)
        playerCounters = findViewById(R.id.playerCounters)
        playerName = findViewById(R.id.tv_player_name)
        profilePopupContainer = findViewById(R.id.profile_popup_container)
        playerCountersPopupContainer = findViewById(R.id.player_counters_popup_container)
        profilesRecyclerView = findViewById(R.id.profiles_recycler_view)
        unloadProfileButton = findViewById(R.id.unload_profile_button)

        // Set up internal listeners
        playerName.setOnClickListener { onPlayerNameClickListener?.invoke() }
        unloadProfileButton.setOnClickListener { onUnloadProfileListener?.invoke() }
        playerCounters.setOnClickListener { onPlayerCountersClickListener?.invoke() }

        // Configure internal components
        profilesRecyclerView.layoutManager = LinearLayoutManager(context)
        lifeCounter.addDismissibleOverlay(profilePopupContainer)
        lifeCounter.addDismissibleOverlay(playerCountersPopupContainer)
    }

    // --- All logic below is moved from the deleted RotatableLayout ---

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

    /**
     * Applies the view's rotation matrix to the given MotionEvent.
     * This method is public to allow for direct testing of the transformation logic.
     * @param event The MotionEvent to transform.
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        // The transformation logic is now in its own testable method.
        transformEvent(event)
        return super.dispatchTouchEvent(event)
    }

    /**
     * Updates the entire player segment's UI based on a Player data object.
     */
    fun updateUI(player: Player, isMassUpdate: Boolean) {
        playerName.text = player.name

        if (isMassUpdate) {
            lifeCounter.setLifeAnimate(player.life)
        } else {
            lifeCounter.life = player.life
        }

        try {
            if (player.color != null) {
                val backgroundColor = player.color.toColorInt()
                this.setBackgroundColor(backgroundColor)
                playerName.setTextColor(if (isColorDark(backgroundColor)) Color.WHITE else Color.BLACK)
            } else {
                this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
                playerName.setTextColor(Color.WHITE)
            }
        } catch (_: Exception) {
            this.setBackgroundColor(ContextCompat.getColor(context, R.color.default_segment_background))
            playerName.setTextColor(Color.WHITE)
        }

        unloadProfileButton.visibility = if (player.profileId != null) VISIBLE else INVISIBLE
    }

    /**
     * Sets the text and view sizes for this segment based on the total number of players.
     */
    fun setViewSizes(lifeSize: Float, nameSize: Float, countersWidth: Int, countersHeight: Int) {
        lifeCounter.setTextSize(TypedValue.COMPLEX_UNIT_PX, lifeSize)
        playerName.setTextSize(TypedValue.COMPLEX_UNIT_PX, nameSize)

        val countersLayoutParams = playerCounters.layoutParams
        countersLayoutParams.width = countersWidth
        countersLayoutParams.height = countersHeight
        playerCounters.layoutParams = countersLayoutParams
    }
}