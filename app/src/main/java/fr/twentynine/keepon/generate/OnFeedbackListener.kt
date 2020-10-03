package fr.twentynine.keepon.generate

/**
 * Copyright (c) Pixplicity, Gene-rate
 * https://github.com/Pixplicity/gene-rate
 */
interface OnFeedbackListener {
    fun onFeedbackTapped()
    fun onRateTapped()
    fun onRequestDismissed(dontAskAgain: Boolean)
}
