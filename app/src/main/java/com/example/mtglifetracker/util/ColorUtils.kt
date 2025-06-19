package com.example.mtglifetracker.util

import android.graphics.Color

/**
 * Determines if a color is "dark" based on its perceived luminance.
 * @return True if the color is dark, false if it is light.
 */
fun isColorDark(color: Int): Boolean {
    val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
    return darkness >= 0.5
}