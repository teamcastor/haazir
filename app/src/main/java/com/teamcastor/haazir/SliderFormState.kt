package com.teamcastor.haazir

import android.view.View
import com.teamcastor.haazir.data.model.AppViewModel

data class SliderFormState(
    val event: String = "in",
    val isLocked: Boolean = AppViewModel.geofenceState.value == null || AppViewModel.geofenceState.value == false,
    val Visibility: Int = View.VISIBLE
)

val SliderFormState.isReversed: Boolean get() = event == "out"