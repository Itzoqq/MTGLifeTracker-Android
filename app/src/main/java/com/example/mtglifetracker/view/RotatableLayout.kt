package com.example.mtglifetracker.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.withSave
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mtglifetracker.R
import com.google.android.material.card.MaterialCardView

class RotatableLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    val lifeCounter: LifeCounterView
    // The 'deltaCounter' TextView reference is now removed.
    val playerName: TextView
    val profilePopupContainer: MaterialCardView
    val profilesRecyclerView: RecyclerView
    val unloadProfileButton: ImageView

    internal var angle: Int = 0

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RotatableLayout,
            0, 0
        ).apply {
            try {
                angle = getInteger(R.styleable.RotatableLayout_angle, 0)
            } finally {
                recycle()
            }
        }

        rotation = 0f
        setWillNotDraw(false)

        inflate(context, R.layout.layout_player_segment, this)

        lifeCounter = findViewById(R.id.lifeCounter)
        // The initialization for 'deltaCounter' is also removed.
        playerName = findViewById(R.id.tv_player_name)
        profilePopupContainer = findViewById(R.id.profile_popup_container)
        profilesRecyclerView = findViewById(R.id.profiles_recycler_view)
        unloadProfileButton = findViewById(R.id.unload_profile_button)
        profilesRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
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

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
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
        val handled = super.dispatchTouchEvent(event)
        return handled
    }
}