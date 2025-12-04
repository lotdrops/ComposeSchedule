package com.example.schedule

import kotlinx.datetime.LocalTime

fun LocalTime.plusMinutes(minutes: Int): LocalTime {
    val totalMinutes = toSecondOfDay() / MINUTE_SECONDS + minutes
    return LocalTime.fromSecondOfDay((totalMinutes * MINUTE_SECONDS) % DAY_SECONDS)
}

const val DAY_HOURS = 24
const val HOUR_MINUTES = 60
const val MINUTE_SECONDS = 60
const val HOUR_SECONDS = 3600
const val DAY_SECONDS = HOUR_SECONDS * DAY_HOURS
