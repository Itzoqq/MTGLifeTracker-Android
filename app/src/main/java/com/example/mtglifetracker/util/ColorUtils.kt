package com.example.mtglifetracker.util

import android.graphics.Color

/**
 * Determines if a given color is "dark" enough to require a light-colored text (e.g., white)
 * for adequate contrast.
 *
 * This function calculates the perceived luminance of the color using a standard formula
 * (based on the Rec. 709 primaries) that weighs the red, green, and blue channels
 * according to how the human eye perceives their brightness.
 *
 * The formula is: `Luminance = (0.299 * Red + 0.587 * Green + 0.114 * Blue) / 255`.
 *
 * A color is considered "dark" if its calculated darkness (1 - Luminance) is 0.5 or greater.
 *
 * @param color The integer representation of the color to be evaluated.
 * @return `true` if the color is dark, `false` if it is light.
 */
fun isColorDark(color: Int): Boolean {
    // Log the input for debugging purposes. The color is logged in hex format for easy identification.
    Logger.d("isColorDark: Checking color #${Integer.toHexString(color).uppercase()}.")

    // Calculate the perceived darkness of the color.
    val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255

    // The threshold of 0.5 is a common choice for determining if a color is dark or light.
    val isDark = darkness >= 0.5
    Logger.d("isColorDark: Calculated darkness = $darkness. Result: ${if (isDark) "DARK" else "LIGHT"}.")

    return isDark
}