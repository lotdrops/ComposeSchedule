package com.example.schedule.placement

import com.example.schedule.EPS
import com.example.schedule.PlacedEvent
import com.example.schedule.ScheduleEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PlacementLogicTest {
    @Test
    fun `PlaceEvents forks for an empty list`() {
        val placedEvents = emptyList<TestEvent>().placeEvents()
        assertEquals(emptyList(), placedEvents)
    }

    @Test
    fun `PlaceEvents contains all events that have a duration`() = allSets { events ->
        val placedEvents = events.placeEvents()
        events.filter { it.startTime <= it.endTime }.assertAllFoundIn(placedEvents)
    }

    @Test
    fun `PlaceEvents returns all events with non zero width`() = allSets { events ->
        val placedEvents = events.placeEvents()
        println("placedEvents:$placedEvents")
        assertTrue { placedEvents.all { it.width.value > 0f } }
    }

    @Test
    fun `PlaceEvents returns a placement with no overlap`() = allSets { events ->
        val placedEvents = events.placeEvents()
        placedEvents.assertNoOverlaps()
    }

    @Test
    fun `PlaceEvents returns no placement beyond bounds`() = allSets { events ->
        val placedEvents = events.placeEvents()
        assertTrue { placedEvents.all { it.x.value + it.width.value <= 0f } }
        assertTrue { placedEvents.all { it.x.value >= 0f } }
    }

    @Test
    fun `Events that start at the time that others end do not overlap`() {
        val placedEvents = NoOverlapTouchingEvents.placeEvents()
        placedEvents.forEach { assertEquals(1f, it.width.value) }
    }

    @Test
    fun `Events that fully overlap are placed next to each other`() {
        val placedEvents = ThreeFullOverlapEvents.placeEvents()
        placedEvents.forEach { assertEquals(1 / 3f, it.width.value) }
    }

    @Test
    fun `In a chain of 3 events, if the third does not overlap the first it is placed below it`() {
        val placedEvents = ChainOfThreeButTwoColumns.placeEvents()
        placedEvents.forEach { assertEquals(1 / 2f, it.width.value) }
    }

    @Test
    fun `Given 2 groups with different sizes, PlaceEvents returns the proper width for each group`() {
        val placedEvents = ThreeColumnsTwoColumns.placeEvents()

        placedEvents assertExactly 3 haveWidth 1 / 3f
        placedEvents assertExactly 2 haveWidth 1 / 2f
    }

    @Test
    fun `A chain of events that is shorter than the max chain is expanded equally`() {
        val placedEvents = OneBigContainsThreeAndTwoThatShouldExpand.placeEvents()

        placedEvents assertExactly 4 haveWidth 1 / 4f
        placedEvents assertExactly 2 haveWidth 3 / 4f / 2f
    }

    @Test
    fun `Two connected chains of events that are shorter than the max chain expanded equally`() {
        val placedEvents = ExpandSubGroupOfEvents.placeEvents()

        placedEvents assertExactly 4 haveWidth 1 / 4f
        placedEvents assertExactly 3 haveWidth 3 / 4f / 2f
    }

    @Test
    fun `Within a subgroup of connected events that expand, one expands more`() {
        val placedEvents = ExpandSubGroupOfEventsOneItemExpandsMore.placeEvents()

        placedEvents assertExactly 5 haveWidth 1 / 5f
        placedEvents assertExactly 3 haveWidth 4 / 5f / 3f
        placedEvents assertExactly 1 haveWidth 4 / 5f * 2 / 3f
    }

    @Test
    fun `Within a subgroup of connected events that expand, there are two paths and both expand equally`() {
        val placedEvents = RelatedPathsCanExpand.placeEvents()

        placedEvents assertExactly 6 haveWidth 1 / 6f
        placedEvents assertExactly 3 haveWidth 5 / 6f / 2f
    }

    @Test
    fun `An event that overlaps with the last event of a chain expands until that event`() {
        val placedEvents = ItemExpandsUntilLastEventOfChain.placeEvents()

        placedEvents assertExactly 3 haveWidth 1 / 3f
        placedEvents assertExactly 1 haveWidth 2 / 3f
    }

    @Test
    fun `Two overlapping events also overlap with the last event of a chain and expand equally until that event`() {
        val placedEvents = SeparatePairExpandsUntilCollision.placeEvents()

        placedEvents assertExactly 4 haveWidth 1 / 4f
        placedEvents assertExactly 2 haveWidth 3 / 4f / 2f
    }

    @Test
    fun `Of a subgroup that could expand one collides and the other expands fully`() {
        val placedEvents = ChainCollidesOnlyOneItemExpands.placeEvents()

        placedEvents assertExactly 5 haveWidth 1 / 4f
        placedEvents assertExactly 1 haveWidth 1 / 4f * 2
    }

    @Test
    fun `A subgroup of 4 events that expand has two paths but all events expand equally`() {
        val placedEvents = EventsWithTwoPathsExpandEqually.placeEvents()

        placedEvents assertExactly 5 haveWidth 1 / 5f
        placedEvents assertExactly 3 haveWidth 4 / 5f / 3f
    }
}

private fun List<TestEvent>.assertAllFoundIn(placedEvents: List<PlacedEvent<TestEvent>>) {
    println(
        "expected:${this.joinToString {
            it.id.toString()
        }}, placed:${placedEvents.joinToString {
            it.event.id
                .toString()
        }}",
    )
    assertEquals(this.size, placedEvents.size, "Events size mismatch for:$this")
    forEach { event ->
        assertTrue("Event $event not found in placed events") {
            placedEvents.any { it.event == event }
        }
    }
}

private fun List<PlacedEvent<*>>.assertNoOverlaps() = forEach { event ->
    forEach { other ->
        if (event != other) {
            assertTrue("Event overlap! ONE:$event, OTHER:$other") { !event.overlapsWith(other) }
        }
    }
}

private fun PlacedEvent<*>.overlapsWith(other: PlacedEvent<*>): Boolean =
    this.collidesInTimeWith(other) && overlapHorizontally(this, other)

private fun PlacedEvent<*>.collidesInTimeWith(
    other: PlacedEvent<*>,
): Boolean = collideInTime(this.event, other.event)

private fun collideInTime(a: ScheduleEvent, b: ScheduleEvent): Boolean {
    val aStart = a.startTime
    val aEnd = a.endTime
    val bStart = b.startTime
    val bEnd = b.endTime
    return aStart < bEnd && bStart < aEnd
}

private fun overlapHorizontally(a: PlacedEvent<*>, b: PlacedEvent<*>): Boolean {
    val aL = a.x.value
    val aR = a.x.value + a.width.value
    val bL = b.x.value
    val bR = b.x.value + b.width.value
    return aL < bR - EPS && bL < aR - EPS
}

private infix fun List<PlacedEvent<TestEvent>>.assertExactly(numberOfEvents: Int) =
    this to numberOfEvents

private infix fun Pair<List<PlacedEvent<TestEvent>>, Int>.haveWidth(widthPercent: Float) =
    assertEquals(
        second,
        first.count { it.width.value in (widthPercent - EPS)..(widthPercent + EPS) },
    )
