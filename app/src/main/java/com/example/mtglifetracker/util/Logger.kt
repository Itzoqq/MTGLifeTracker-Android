package com.example.mtglifetracker.util

import timber.log.Timber

object Logger {

    // Predefined tags for tests
    private const val UNIT_TAG = "UNIT_TEST"
    private const val INSTRUMENTED_TAG = "INSTRUMENTED_TEST"

    // For application code
    fun i(message: String, vararg args: Any?) = Timber.i(message, *args)
    fun d(message: String, vararg args: Any?) = Timber.d(message, *args)
    fun w(message: String, vararg args: Any?) = Timber.w(message, *args)
    fun e(t: Throwable?, message: String, vararg args: Array<out Any?>) = Timber.e(t, message, *args)

    // For unit tests
    fun unit(message: String, vararg args: Any?) = Timber.tag(UNIT_TAG).d(message, *args)

    // For instrumented tests
    fun instrumented(message: String, vararg args: Any?) = Timber.tag(INSTRUMENTED_TAG).d(message, *args)
}