package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

class CommanderDamageDialogFragment : DialogFragment() {

    private val gameViewModel: GameViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val playerCount = gameViewModel.gameState.value.playerCount
        val angle = requireArguments().getInt(ARG_ANGLE)

        val layoutId = when (playerCount) {
            2 -> when (angle) {
                0 -> R.layout.dialog_commander_damage_2_player
                180 -> R.layout.dialog_commander_damage_2_player_rotated_180
                else -> -1
            }
            3 -> when (angle) {
                90 -> R.layout.dialog_commander_damage_3_player_rotated_right
                -90 -> R.layout.dialog_commander_damage_3_player_rotated_left
                180 -> R.layout.dialog_commander_damage_3_player_rotated_180
                else -> -1
            }
            4 -> when (angle) {
                90 -> R.layout.dialog_commander_damage_4_player_rotated_right
                -90 -> R.layout.dialog_commander_damage_4_player_rotated_left
                else -> -1
            }
            5 -> when (angle) {
                90 -> R.layout.dialog_commander_damage_5_player_rotated_right
                -90 -> R.layout.dialog_commander_damage_5_player_rotated_left
                else -> -1
            }
            6 -> when (angle) {
                90 -> R.layout.dialog_commander_damage_6_player_rotated_right
                -90 -> R.layout.dialog_commander_damage_6_player_rotated_left
                else -> -1
            }
            else -> -1
        }


        if (layoutId == -1) {
            dismiss()
            return null
        }
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val targetPlayerIndex = requireArguments().getInt(ARG_TARGET_PLAYER_INDEX)
        val allPlayers = gameViewModel.gameState.value.players
        val targetPlayer = allPlayers.find { it.playerIndex == targetPlayerIndex }!!
        val playerCount = allPlayers.size
        val angle = requireArguments().getInt(ARG_ANGLE)

        val titleView: TextView = view.findViewById(R.id.tv_dialog_content_title)
        titleView.text = getString(R.string.player_commander_damage_title, targetPlayer.name)
        if (titleView is VerticalTextView) {
            titleView.isTopDown = (angle == 90)
        }

        when (playerCount) {
            2 -> setup2PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            3 -> setup3PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            4 -> setup4PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            5 -> setup5PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            6 -> setup6PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
        }

        // When the dialog's background is clicked, hide all decrement buttons.
        view.setOnClickListener {
            if (it is ViewGroup) {
                hideAllDecrementButtons(it)
            }
        }
    }

    /**
     * Recursively traverses a ViewGroup to find and hide all visible decrement buttons.
     */
    private fun hideAllDecrementButtons(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child.id == R.id.iv_decrement_button) {
                if (child.isVisible) {
                    child.visibility = View.INVISIBLE
                }
            } else if (child is ViewGroup) {
                hideAllDecrementButtons(child)
            }
        }
    }

    private fun setup2PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val playersForLayout = allPlayers.sortedBy { it.playerIndex }
        val column = view.findViewById<LinearLayout>(R.id.damage_column)
        val itemViews = listOfNotNull(
            column.getChildAt(0), column.getChildAt(1)
        )
        bindPlayerLayout(itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup3PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val player1 = allPlayers.find { it.playerIndex == 0 }!!
        val player2 = allPlayers.find { it.playerIndex == 1 }!!
        val player3 = allPlayers.find { it.playerIndex == 2 }!!
        val playersForLayout = listOf(player1, player2, player3)

        val column = view.findViewById<LinearLayout>(R.id.damage_column)
        val topView = column.getChildAt(0)
        val bottomRow = column.getChildAt(1) as LinearLayout
        val bottomLeftView = bottomRow.getChildAt(0)
        val bottomRightView = bottomRow.getChildAt(1)

        val itemViews = listOfNotNull(
            topView, bottomLeftView, bottomRightView
        )
        bindPlayerLayout(itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup4PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val player1 = allPlayers.find { it.playerIndex == 0 }!!
        val player2 = allPlayers.find { it.playerIndex == 1 }!!
        val player3 = allPlayers.find { it.playerIndex == 2 }!!
        val player4 = allPlayers.find { it.playerIndex == 3 }!!

        val playersForLayout = listOf(player1, player3, player2, player4)

        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)

        val itemViews = listOfNotNull(
            leftColumn.getChildAt(0), leftColumn.getChildAt(1),
            rightColumn.getChildAt(0), rightColumn.getChildAt(1)
        )
        bindPlayerLayout(itemViews, playersForLayout, targetPlayerIndex, angle)
    }


    private fun setup5PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val playersForLayout = allPlayers.sortedBy { it.playerIndex }

        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)

        val itemViews = listOfNotNull(
            leftColumn.getChildAt(0), leftColumn.getChildAt(1),
            rightColumn.getChildAt(0), rightColumn.getChildAt(1), rightColumn.getChildAt(2)
        )
        bindPlayerLayout(itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup6PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        val player1 = allPlayers.find { it.playerIndex == 0 }!!
        val player2 = allPlayers.find { it.playerIndex == 1 }!!
        val player3 = allPlayers.find { it.playerIndex == 2 }!!
        val player4 = allPlayers.find { it.playerIndex == 3 }!!
        val player5 = allPlayers.find { it.playerIndex == 4 }!!
        val player6 = allPlayers.find { it.playerIndex == 5 }!!

        val playersForLayout = listOf(player1, player3, player5, player2, player4, player6)

        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)

        val itemViews = listOfNotNull(
            leftColumn.getChildAt(0), leftColumn.getChildAt(1), leftColumn.getChildAt(2),
            rightColumn.getChildAt(0), rightColumn.getChildAt(1), rightColumn.getChildAt(2)
        )
        bindPlayerLayout(itemViews, playersForLayout, targetPlayerIndex, angle)
    }


    private fun bindPlayerLayout(itemViews: List<View>, playersForLayout: List<Player>, targetPlayerIndex: Int, angle: Int) {
        lifecycleScope.launch {
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                val damageMap = damages.associate { it.sourcePlayerIndex to it.damage }

                itemViews.forEachIndexed { index, itemView ->
                    val player = playersForLayout.getOrNull(index)
                    if (player != null) {
                        itemView.visibility = View.VISIBLE
                        val damage = damageMap[player.playerIndex] ?: 0
                        bindDamageView(itemView, player, damage, targetPlayerIndex, angle)
                    } else {
                        itemView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }


    private fun bindDamageView(itemView: View, player: Player, damage: Int, targetPlayerIndex: Int, angle: Int) {
        val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        val decrementButton: ImageView = itemView.findViewById(R.id.iv_decrement_button)

        // --- ALL FIXES ARE HERE ---

        // 1. Rotate the number in the box for all angles
        damageAmount.rotation = angle.toFloat()

        // 2. Rotate the standard TextView nickname for 0 and 180 degrees
        if (opponentName !is VerticalTextView) {
            opponentName.rotation = angle.toFloat()
        }

        // 3. Set the correct orientation for the VerticalTextView nickname in +/-90 degree layouts
        (opponentName as? VerticalTextView)?.isTopDown = (angle == 90)


        opponentName.text = player.name

        (damageAmount.background as? GradientDrawable)?.let { background ->
            val color = player.color?.toColorInt()
                ?: ContextCompat.getColor(requireContext(), R.color.default_segment_background)
            background.setColor(color)
        }

        if (player.playerIndex == targetPlayerIndex) {
            damageAmount.text = getString(R.string.me)
            itemView.alpha = 0.6f
            // Disable all clicks for the "Me" item
            damageAmount.setOnClickListener(null)
            damageAmount.setOnLongClickListener(null)
            decrementButton.setOnClickListener(null)
            decrementButton.visibility = View.INVISIBLE // Hide button for "Me"
        } else {
            damageAmount.text = damage.toString()
            itemView.alpha = 1.0f

            // Click the box to increment damage
            damageAmount.setOnClickListener {
                decrementButton.visibility = View.INVISIBLE
                gameViewModel.incrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }

            // Long-click the box to show the decrement button
            damageAmount.setOnLongClickListener {
                decrementButton.visibility = View.VISIBLE
                true
            }

            // Click the button to decrement damage
            decrementButton.setOnClickListener {
                gameViewModel.decrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val size = (displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(size, size)
            window.setGravity(Gravity.CENTER)
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    companion object {
        const val TAG = "CommanderDamageDialogFragment"
        private const val ARG_TARGET_PLAYER_INDEX = "target_player_index"
        private const val ARG_ANGLE = "angle"

        fun newInstance(targetPlayerIndex: Int, angle: Int): CommanderDamageDialogFragment {
            return CommanderDamageDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TARGET_PLAYER_INDEX, targetPlayerIndex)
                    putInt(ARG_ANGLE, angle)
                }
            }
        }
    }
}