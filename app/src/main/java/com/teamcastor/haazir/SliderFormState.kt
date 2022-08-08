package com.teamcastor.haazir

import android.view.View

data class SliderFormState(
    val event: String = "in",
    val isLocked: Boolean = false,
    val Visibility: Int = View.VISIBLE
)

val SliderFormState.isReversed: Boolean get() = event == "out"