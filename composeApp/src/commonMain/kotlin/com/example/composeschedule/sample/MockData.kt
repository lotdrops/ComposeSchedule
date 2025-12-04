package com.example.composeschedule.sample

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalTime

internal fun mockEvents1(): ImmutableList<AppEvent> = persistentListOf(
    AppEvent(1, "Event 1", LocalTime(10, 0), LocalTime(11, 0)),
    AppEvent(2, "Event 2", LocalTime(10, 15), LocalTime(13, 0)),
    AppEvent(3, "Event 3", LocalTime(14, 0), LocalTime(15, 0)),
)
internal fun mockEvents2(): ImmutableList<AppEvent> = persistentListOf(
    AppEvent(0, "Event 1", LocalTime(9, 0), LocalTime(9, 5)),
    AppEvent(1, "Event 1", LocalTime(10, 0), LocalTime(11, 0)),
    AppEvent(2, "Event 2", LocalTime(10, 15), LocalTime(13, 0)),
    AppEvent(3, "Event 3", LocalTime(14, 0), LocalTime(15, 0)),
    AppEvent(4, "Event 4", LocalTime(9, 25), LocalTime(10, 25)),
)
