package com.example.schedule

import kotlinx.datetime.LocalTime
import kotlin.jvm.JvmInline
import kotlin.math.max
import kotlin.time.DurationUnit
import kotlin.time.toDuration

interface ScheduleEvent {
    val startTime: LocalTime
    val endTime: LocalTime

    fun collidesWith(other: ScheduleEvent): Boolean =
        this.startTime < other.endTime && this.endTime > other.startTime
}

/**
 * We can specify a minimum height for events so that they are readable, or clickable.
 * When we do this, the event is laid out bigger than it should be, according to its duration.
 */
internal data class MinHeightScheduleEvent<T : ScheduleEvent>(
    val minDurationMinutes: Int,
    val event: T,
) : ScheduleEvent {
    override val startTime: LocalTime get() = event.startTime
    override val endTime: LocalTime get() {
        val durationMinutes = (event.endTime.toSecondOfDay() - event.startTime.toSecondOfDay())
            .toDuration(DurationUnit.SECONDS)
            .inWholeMinutes
            .toInt()
        return event.startTime.plusMinutes(max(minDurationMinutes, durationMinutes))
    }
}

@ConsistentCopyVisibility
internal data class PlacedEvent<T : ScheduleEvent> private constructor(
    val event: T,
    val x: Percent,
    val width: Percent,
) {
    companion object {
        operator fun <T : ScheduleEvent> invoke(
            event: T,
            xPercent: Float,
            widthPercent: Float,
        ): PlacedEvent<T> {
            require(xPercent + widthPercent <= 1f + EPS) {
                "xPercent + widthPercent must be <= 1"
            }
            return PlacedEvent(event, Percent(xPercent), Percent(widthPercent))
        }
    }
}

@JvmInline
internal value class Percent private constructor(val value: Float) {
    companion object {
        operator fun invoke(value: Float): Percent {
            require(value in 0f..1f) { "Percent must be in [0, 1]" }
            return Percent(value)
        }
    }
}

internal const val EPS = 1e-6f
