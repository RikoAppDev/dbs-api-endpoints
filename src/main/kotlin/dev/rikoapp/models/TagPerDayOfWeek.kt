package dev.rikoapp.models

import kotlinx.serialization.Serializable

@Serializable
data class TagPerDayOfWeek(
    val monday: Float,
    val tuesday: Float,
    val wednesday: Float,
    val thursday: Float,
    val friday: Float,
    val saturday: Float,
    val sunday: Float,
)
