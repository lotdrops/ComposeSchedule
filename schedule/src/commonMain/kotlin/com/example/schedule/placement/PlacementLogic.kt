package com.example.schedule.placement

import com.example.schedule.PlacedEvent
import com.example.schedule.ScheduleEvent
import kotlinx.datetime.LocalTime
import kotlin.jvm.JvmInline

internal fun <T : ScheduleEvent> List<T>.placeEvents(): List<PlacedEvent<T>> = this
    .filter {
        it.endTime >= it.startTime
    }.sortedWith(compareBy({ it.startTime }, { it.endTime }))
    .fold(PlacingParams.empty<T>()) { ongoing, event ->
        if (event.startTime >= ongoing.collisionGroupMaxTime &&
            ongoing.collisionGroup.isNotEmpty()
        ) {
            ongoing.createNewGroup(event)
        } else {
            ongoing.addToCollisionGroup(event)
        }
    }.placeRemainingEvents()
    .placedEvents

/**
 * Class to accumulate values while iterating events for placement.
 *
 * A collision group (connected components) is a group of events where all events collide in time
 * with at least one event of the group.
 *
 * We place events only when each collision group is completed.
 */
private data class PlacingParams<T : ScheduleEvent>(
    val placedEvents: List<PlacedEvent<T>>,
    val collisionGroup: List<Column<T>>,
    val collisionGroupMaxTime: LocalTime,
) {
    fun createNewGroup(event: T): PlacingParams<T> = placeCollisionGroup(event)

    fun placeRemainingEvents() = placeCollisionGroup(null)

    private fun placeCollisionGroup(newEvent: T?) = PlacingParams(
        placedEvents = placedEvents + collisionGroup.toPlacedEvents(),
        collisionGroup = newEvent?.let { listOf(Column(listOf(it))) } ?: emptyList(),
        collisionGroupMaxTime = newEvent?.endTime ?: LocalTime(0, 0),
    )

    fun addToCollisionGroup(event: T): PlacingParams<T> = copy(
        collisionGroup = collisionGroup.addToProperColumn(event),
        collisionGroupMaxTime = maxOf(collisionGroupMaxTime, event.endTime),
    )

    companion object {
        fun <T : ScheduleEvent> empty() =
            PlacingParams<T>(emptyList(), emptyList(), LocalTime(0, 0))
    }
}

private fun <T : ScheduleEvent> List<Column<T>>.toPlacedEvents(): List<PlacedEvent<T>> {
    val placedEvents = mutableMapOf<T, PlacedEvent<T>>()
    val subGraphsOfCollision = buildSubGraphsOfCollision()
    val minWidth = 1f / this.size

    val placeEvent: (event: T, x: Float, width: Float) -> Unit = { event, x, width ->
        placedEvents.put(event, PlacedEvent(event, x, width))
    }
    val placeMinWidthEvents: List<T>.() -> Unit = {
        forEachIndexed { index, event ->
            if (event in placedEvents) return@forEachIndexed
            placeEvent(event, index * minWidth, minWidth)
        }
    }

    // handle all sub-graphs that match the number of columns
    subGraphsOfCollision.filter { it.size == this.size }.forEach { subGraph ->
        subGraph.placeMinWidthEvents()
    }

    // handle all sub-graphs that should expand
    // we sort by length because longer chains might limit some elements of shorter chains
    subGraphsOfCollision
        .filter { it.size < this.size }
        .sortedByDescending { it.size }
        .forEach { subGraph ->
            // for this subgraph, we check how much it can grow to the right:
            // if we find an element, it must have been placed, and its X will be our limit
            // if there is no colliding element, we can grow to the max, 1f
            val rightClosestCollidingWithLastEvent = this
                .drop(subGraph.size + 1)
                .firstNotNullOfOrNull { column ->
                    column.elements.firstOrNull { it.collidesWith(subGraph.last()) }
                }
            val rightMostLimit =
                rightClosestCollidingWithLastEvent?.let { placedEvents[it]?.x?.value } ?: 1f

            // now we iterate from right to left finding the first element placed or that collides with other events to its right
            val lastNonExpandableItemPosition = subGraph
                .dropLast(1)
                .indexOfLastIndexed {
                    index,
                    event,
                    ->
                    // if that event collides (right side of the or) we could use this as a hint that a different placement order might, in some cases, yield a better result
                    event in placedEvents ||
                        this[index + 1].elements.any { elementOfNextColumn ->
                            elementOfNextColumn !=
                                subGraph[index + 1] &&
                                elementOfNextColumn.collidesWith(event)
                        }
                }.coerceAtLeast(0)

            // we place events before the first expandable to minWidth, and expand events after
            subGraph.take(lastNonExpandableItemPosition).placeMinWidthEvents()
            val expandableItems = subGraph.drop(lastNonExpandableItemPosition)
            var firstValidX = lastNonExpandableItemPosition * minWidth
            expandableItems.forEachIndexed { index, event ->
                val maxExpansionWidth =
                    (rightMostLimit - firstValidX) / (expandableItems.size - index)
                // we check how much it can grow until it collides
                // if we find an element, it must have been placed, and its X will be our limit
                // if there is no colliding element, we can grow to the max, 1f
                val rightClosestCollidingWithLastEvent = this
                    .drop(index + 1)
                    .firstNotNullOfOrNull { column ->
                        column.elements.firstOrNull {
                            it !in expandableItems &&
                                it.collidesWith(
                                    event,
                                )
                        }
                    }
                val rightLimit =
                    rightClosestCollidingWithLastEvent?.let { placedEvents[it]?.x?.value } ?: 1f
                val width = minOf(maxExpansionWidth, rightLimit - firstValidX)
                placeEvent(event, firstValidX, width)
                firstValidX += width
            }
        }

    return placedEvents.values.toList()
}

private fun <T : ScheduleEvent> List<Column<T>>.addToProperColumn(event: T): List<Column<T>> {
    var added = false
    val result = map { column ->
        if (!added && event.startTime >= column.last().endTime) {
            added = true
            Column((column.elements + event))
        } else {
            column
        }
    }
    return if (added) result else this + Column(listOf(event))
}

@JvmInline
value class Column<T : ScheduleEvent>(val elements: List<T>) {
    fun last() = elements.last()
}

fun <T> List<T>.indexOfLastIndexed(predicate: (Int, T) -> Boolean): Int {
    val iterator = this.listIterator(size)
    var index = size
    while (iterator.hasPrevious()) {
        index = size - 1
        if (predicate(index, iterator.previous())) {
            return iterator.nextIndex()
        }
    }
    return -1
}
