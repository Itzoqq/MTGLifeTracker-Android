package com.example.mtglifetracker.view

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.mtglifetracker.R
import com.example.mtglifetracker.model.Player
import com.example.mtglifetracker.util.Logger
import com.example.mtglifetracker.viewmodel.GameViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A [DialogFragment] for displaying and editing commander damage for a specific player.
 *
 * This dialog is highly dynamic, capable of inflating different layouts based on the
 * number of players in the game and the rotation angle of the player segment that opened it.
 * It observes the [GameViewModel] to display real-time damage updates and provides UI controls
 * for incrementing and decrementing damage totals.
 */
class CommanderDamageDialogFragment : DialogFragment() {

    // Get a reference to the shared GameViewModel.
    private val gameViewModel: GameViewModel by activityViewModels()

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * This method selects and inflates the correct XML layout file based on the number of
     * players and the rotation angle passed in the fragment's arguments. This ensures that
     * the dialog is oriented correctly for the user who opened it.
     */
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val playerCount = gameViewModel.gameState.value.playerCount
        val angle = requireArguments().getInt(ARG_ANGLE)
        Logger.i("CommanderDamageDialog: onCreateView. Player count = $playerCount, Angle = $angle.")

        // Determine the correct layout resource ID based on the game state.
        val layoutId = when (playerCount) {
            2 -> when (angle) {
                0 -> R.layout.dialog_commander_damage_2_player
                180 -> R.layout.dialog_commander_damage_2_player_rotated_180
                else -> -1 // Invalid configuration
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

        // If the layout ID is invalid, log an error and dismiss the dialog to prevent a crash.
        if (layoutId == -1) {
            Logger.e(null, "CommanderDamageDialog: No valid layout found for player count $playerCount and angle $angle. Dismissing dialog.")
            dismiss()
            return null
        }

        Logger.d("CommanderDamageDialog: Inflating layout ID $layoutId.")
        return inflater.inflate(layoutId, container, false)
    }

    /**
     * Called immediately after `onCreateView()` has returned, but before any saved state
     * has been restored in to the view. This is where UI setup and data binding occurs.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d("CommanderDamageDialog: onViewCreated.")

        // Retrieve arguments passed to the dialog.
        val targetPlayerIndex = requireArguments().getInt(ARG_TARGET_PLAYER_INDEX)
        val allPlayers = gameViewModel.gameState.value.players
        val targetPlayer = allPlayers.find { it.playerIndex == targetPlayerIndex }!!
        val playerCount = allPlayers.size
        val angle = requireArguments().getInt(ARG_ANGLE)
        Logger.d("CommanderDamageDialog: TargetPlayerIndex=$targetPlayerIndex, PlayerCount=$playerCount, Angle=$angle.")

        // Set the dialog title.
        val titleView: TextView = view.findViewById(R.id.tv_dialog_content_title)
        titleView.text = getString(R.string.player_commander_damage_title, targetPlayer.name)
        // Handle custom vertical text view orientation.
        if (titleView is VerticalTextView) {
            titleView.isTopDown = (angle == 90)
        }

        // Delegate to the appropriate setup function based on player count.
        when (playerCount) {
            2 -> setup2PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            3 -> setup3PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            4 -> setup4PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            5 -> setup5PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
            6 -> setup6PlayerLayout(view, allPlayers, targetPlayerIndex, angle)
        }

        // Set a click listener on the root view to hide decrement buttons when clicking the background.
        view.setOnClickListener {
            if (it is ViewGroup) {
                Logger.d("CommanderDamageDialog: Background clicked. Hiding all decrement buttons.")
                hideAllDecrementButtons(it)
            }
        }
    }

    /**
     * Recursively traverses a [ViewGroup] and hides any visible decrement buttons.
     */
    private fun hideAllDecrementButtons(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child.id == R.id.iv_decrement_button && child.isVisible) {
                child.visibility = View.INVISIBLE
            } else if (child is ViewGroup) {
                // If the child is another layout, search it recursively.
                hideAllDecrementButtons(child)
            }
        }
    }

    // --- Layout-Specific Setup Functions ---

    private fun setup2PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        Logger.d("setup2PlayerLayout: Configuring UI for 2 players.")
        val playersForLayout = allPlayers.sortedBy { it.playerIndex }
        val column = view.findViewById<LinearLayout>(R.id.damage_column)
        val itemViews = listOfNotNull(column.getChildAt(0), column.getChildAt(1))
        bindPlayerLayout(view, itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup3PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        Logger.d("setup3PlayerLayout: Configuring UI for 3 players.")
        val player1 = allPlayers.find { it.playerIndex == 0 }!!
        val player2 = allPlayers.find { it.playerIndex == 1 }!!
        val player3 = allPlayers.find { it.playerIndex == 2 }!!
        val playersForLayout = listOf(player1, player2, player3)

        val column = view.findViewById<LinearLayout>(R.id.damage_column)
        val topView = column.getChildAt(0)
        val bottomRow = column.getChildAt(1) as LinearLayout
        val bottomLeftView = bottomRow.getChildAt(0)
        val bottomRightView = bottomRow.getChildAt(1)

        val itemViews = listOfNotNull(topView, bottomLeftView, bottomRightView)
        bindPlayerLayout(view, itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup4PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        Logger.d("setup4PlayerLayout: Configuring UI for 4 players.")
        val player1 = allPlayers.find { it.playerIndex == 0 }!!
        val player2 = allPlayers.find { it.playerIndex == 1 }!!
        val player3 = allPlayers.find { it.playerIndex == 2 }!!
        val player4 = allPlayers.find { it.playerIndex == 3 }!!
        // The order here matches the visual layout in the XML (top-left, bottom-left, top-right, bottom-right).
        val playersForLayout = listOf(player1, player3, player2, player4)

        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)

        val itemViews = listOfNotNull(leftColumn.getChildAt(0), leftColumn.getChildAt(1), rightColumn.getChildAt(0), rightColumn.getChildAt(1))
        bindPlayerLayout(view, itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup5PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        Logger.d("setup5PlayerLayout: Configuring UI for 5 players.")
        val playersForLayout = allPlayers.sortedBy { it.playerIndex }
        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)
        val itemViews = listOfNotNull(leftColumn.getChildAt(0), leftColumn.getChildAt(1), rightColumn.getChildAt(0), rightColumn.getChildAt(1), rightColumn.getChildAt(2))
        bindPlayerLayout(view, itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    private fun setup6PlayerLayout(view: View, allPlayers: List<Player>, targetPlayerIndex: Int, angle: Int) {
        Logger.d("setup6PlayerLayout: Configuring UI for 6 players.")
        // The order here matches the visual layout in the XML.
        val playersForLayout = allPlayers.sortedBy { it.playerIndex }.let {
            listOf(it[0], it[2], it[4], it[1], it[3], it[5])
        }
        val leftColumn = view.findViewById<LinearLayout>(R.id.left_column)
        val rightColumn = view.findViewById<LinearLayout>(R.id.right_column)
        val itemViews = listOfNotNull(leftColumn.getChildAt(0), leftColumn.getChildAt(1), leftColumn.getChildAt(2), rightColumn.getChildAt(0), rightColumn.getChildAt(1), rightColumn.getChildAt(2))
        bindPlayerLayout(view, itemViews, playersForLayout, targetPlayerIndex, angle)
    }

    /**
     * Binds the player data to the list of item views. This function launches a coroutine
     * to collect the latest damage data and then calls [bindDamageView] for each item.
     */
    private fun bindPlayerLayout(rootView: View, itemViews: List<View>, playersForLayout: List<Player>, targetPlayerIndex: Int, angle: Int) {
        Logger.d("bindPlayerLayout: Starting to bind ${itemViews.size} item views.")
        lifecycleScope.launch {
            // Collect the latest damage data for the target player.
            gameViewModel.getCommanderDamageForPlayer(targetPlayerIndex).collectLatest { damages ->
                Logger.d("bindPlayerLayout: Damage data updated. Found ${damages.size} entries.")
                val damageMap = damages.associateBy { it.sourcePlayerIndex }

                itemViews.forEachIndexed { index, itemView ->
                    val player = playersForLayout.getOrNull(index)
                    if (player != null) {
                        itemView.visibility = View.VISIBLE
                        val damage = damageMap[player.playerIndex]?.damage ?: 0
                        bindDamageView(rootView, itemView, player, damage, targetPlayerIndex, angle)
                    } else {
                        // This case handles layouts with empty slots (e.g., 5-player layout).
                        Logger.d("bindPlayerLayout: No player data for index $index. Hiding item view.")
                        itemView.visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    /**
     * Binds a single player's data to a single item view, setting up text, colors, and listeners.
     */
    private fun bindDamageView(rootView: View, itemView: View, player: Player, damage: Int, targetPlayerIndex: Int, angle: Int) {
        Logger.d("bindDamageView: Binding player '${player.name}' (damage: $damage) to an item view.")
        // Get references to the views inside the item layout.
        val opponentName: TextView = itemView.findViewById(R.id.tv_opponent_name)
        val damageAmount: TextView = itemView.findViewById(R.id.tv_commander_damage)
        val decrementButton: ContinuousDecrementView = itemView.findViewById(R.id.iv_decrement_button)

        // Apply rotation to the text elements.
        damageAmount.rotation = angle.toFloat()
        (opponentName as? VerticalTextView)?.isTopDown = (angle == 90)
        if (opponentName !is VerticalTextView) {
            opponentName.rotation = angle.toFloat()
        }

        opponentName.text = player.name

        // Set the background color of the damage counter from the player's profile color.
        (damageAmount.background as? GradientDrawable)?.let { background ->
            val color = player.color?.toColorInt() ?: ContextCompat.getColor(requireContext(), R.color.default_segment_background)
            background.setColor(color)
        }

        // Special handling if this item represents the target player themselves.
        if (player.playerIndex == targetPlayerIndex) {
            damageAmount.text = getString(R.string.me)
            itemView.alpha = 0.6f // Visually disable it.
            damageAmount.setOnClickListener(null)
            damageAmount.setOnLongClickListener(null)
            decrementButton.onDecrementListener = null
            decrementButton.visibility = View.INVISIBLE
        } else {
            // Standard binding for an opponent.
            damageAmount.text = damage.toString()
            itemView.alpha = 1.0f

            // Single click increments damage.
            damageAmount.setOnClickListener {
                Logger.d("bindDamageView: Increment damage for player '${player.name}'.")
                if (rootView is ViewGroup) hideAllDecrementButtons(rootView)
                gameViewModel.incrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }

            // Long click reveals the decrement button.
            damageAmount.setOnLongClickListener {
                Logger.d("bindDamageView: Long-click on player '${player.name}'.")
                if (rootView is ViewGroup) hideAllDecrementButtons(rootView)
                if (damage > 0) {
                    Logger.d("bindDamageView: Damage is > 0, showing decrement button.")
                    decrementButton.visibility = View.VISIBLE
                }
                true
            }

            // Set the listener for the continuous decrement button.
            decrementButton.onDecrementListener = {
                gameViewModel.decrementCommanderDamage(player.playerIndex, targetPlayerIndex)
            }

            // Hide the decrement button automatically if damage drops to 0.
            if (damage <= 0 && decrementButton.isVisible) {
                Logger.d("bindDamageView: Damage for '${player.name}' is 0. Hiding decrement button.")
                decrementButton.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * Called when the fragment's dialog is started. This is where we can set the
     * dialog's size and background properties.
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            Logger.d("CommanderDamageDialog: onStart. Setting dialog window properties.")
            val displayMetrics = resources.displayMetrics
            // Set dialog size to be a square, 90% of the screen width.
            val size = (displayMetrics.widthPixels * 0.90).toInt()
            window.setLayout(size, size)
            window.setGravity(Gravity.CENTER)
            // Make the dialog's background transparent to allow for rounded corners in the layout.
            window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    /**
     * Companion object to provide a standardized factory method for creating instances
     * of this fragment.
     */
    companion object {
        const val TAG = "CommanderDamageDialogFragment"
        private const val ARG_TARGET_PLAYER_INDEX = "target_player_index"
        private const val ARG_ANGLE = "angle"

        /**
         * Creates a new instance of [CommanderDamageDialogFragment] with the required arguments.
         *
         * @param targetPlayerIndex The index of the player for whom to show the damage.
         * @param angle The rotation angle of the parent view.
         * @return A new fragment instance.
         */
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