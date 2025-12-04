package com.example.schedule.placement

import com.example.schedule.ScheduleEvent
import kotlinx.datetime.LocalTime

internal data class TestEvent(
    val id: Int,
    override val startTime: LocalTime,
    override val endTime: LocalTime,
) : ScheduleEvent {
    constructor(id: Int, startMin: Int, endMin: Int) : this(
        id,
        LocalTime(hour = startMin / 60, minute = startMin % 60),
        LocalTime(hour = endMin / 60, minute = endMin % 60),
    )
}

internal fun allSets(
    test: (List<TestEvent>) -> Unit,
): Unit = allEventSets().forEach { (name, set) ->
    try {
        test(set)
    } catch (t: Throwable) {
        throw AssertionError("Failed test for set: $name", t)
    }
}

/*
 * A no-size event, and a full width event
 *
 * 0
 * +-------------------------------------------------------------------------------------------+
 * |                                         ev(0, 30)                                         |
 * +-------------------------------------------------------------------------------------------+
 */
internal val TwoEventsOneEmpty = listOfEvents(Ev(10, 10), Ev(10, 60))

/*
 * A chain of 3 non-overlapping, adjacent events
 *
 * +-------------------------------------------------------------------------------------------+
 * |                                         ev(0, 30)                                         |
 * +-------------------------------------------------------------------------------------------+
 * |                                         ev(30, 60)                                        |
 * +-------------------------------------------------------------------------------------------+
 * |                                         ev(60, 90)                                        |
 * +-------------------------------------------------------------------------------------------+
 */
internal val NoOverlapTouchingEvents = listOfEvents(Ev(0, 30), Ev(30, 60), Ev(60, 90))

/*
 * 3 events fully overlapping.
 * They should be placed side-by-side in 3 columns.
 *
 * +-----------------------------+ +-----------------------------+ +-----------------------------+
 * |          ev(0, 60)          | |          ev(0, 60)          | |          ev(0, 60)          |
 * |                             | |                             | |                             |
 * |                             | |                             | |                             |
 * |                             | |                             | |                             |
 * +-----------------------------+ +-----------------------------+ +-----------------------------+
 */
internal val ThreeFullOverlapEvents = listOfEvents(Ev(0, 60), Ev(0, 60), Ev(0, 60))

/*
 * 3 events that overlap in a chain. Since the first does not overlap with the third, only two
 * columns are needed.
 *
 * +-------------------------------------------+
 * |                 ev(0, 40)                 |
 * |                                           |
 * |                                           | +-------------------------------------------+
 * |                                           | |                ev(20, 60)                 |
 * |                                           | |                                           |
 * +-------------------------------------------+ |                                           |
 * +-------------------------------------------+ |                                           |
 * |                ev(40, 80)                 | |                                           |
 * |                                           | +-------------------------------------------+
 * |                                           |
 * |                                           |
 * +-------------------------------------------+
 */
internal val ChainOfThreeButTwoColumns = listOfEvents(Ev(0, 40), Ev(20, 60), Ev(40, 80))

/*
 * A group of 3 overlapping events followed by a separate group of 2 overlapping events
 *
 * +----------------------------+
 * |         ev(0, 30)          |
 * |                            | +----------------------------+
 * |                            | |         ev(10, 40)         |
 * |                            | |                            | +----------------------------+
 * +----------------------------+ |                            | |         ev(20, 50)         |
 *                                |                            | |                            |
 *                                +----------------------------+ |                            |
 *                                                               |                            |
 *                                                               +----------------------------+
 *
 * +--------------------------------------------+
 * |                ev(100, 120)                |
 * |                                            | +--------------------------------------------+
 * |                                            | |                ev(110, 140)                |
 * +--------------------------------------------+ |                                            |
 *                                                |                                            |
 *                                                +--------------------------------------------+
 */
internal val ThreeColumnsTwoColumns = listOfEvents(
    Ev(0, 30),
    Ev(10, 40),
    Ev(20, 50),
    Ev(100, 120),
    Ev(110, 140),
)

/*
 * A long event collides with a group of two events and then another group of three.
 * The long event and the group of 3 take up 1/4th of the total width.
 *
 * Events 2 and 3 should expand, and take up 1/2 * 3/4 of the total width
 *
 * +--------------------+
 * |     ev(0, 90)      |
 * |                    | +--------------------------------+ +-------------------------------+
 * |                    | |           ev(10, 20)           | |           ev(10, 20)          |
 * |                    | +--------------------------------+ +-------------------------------+
 * |                    |
 * |                    | +--------------------+ +--------------------+
 * |                    | | ev(40, 60)         | | ev(40, 60)         |
 * |                    | |                    | |                    | +--------------------+
 * |                    | |                    | |                    | | ev(50, 70)         |
 * |                    | +--------------------+ +--------------------+ |                    |
 * |                    |                                               |                    |
 * |                    |                                               +--------------------+
 * |                    |
 * |                    |
 * +--------------------+
 */
internal val OneBigContainsThreeAndTwoThatShouldExpand = listOfEvents(
    Ev(0, 90),
    Ev(10, 20),
    Ev(10, 20),
    Ev(40, 60),
    Ev(40, 60),
    Ev(50, 70),
)

/*
 * A long event collides with two subgroups.
 * One subgroup has 3 events, forcing events to take up 1/4th of the width.
 * The other subgroup has 3 events but only two columns, so these should expand to 3/4 * 1/2
 *
 * +--------------------+
 * |     ev(0, 200)     |
 * |                    | +--------------------------------+
 * |                    | |           ev(10, 30)           | +--------------------------------+
 * |                    | |                                | |           ev(15, 20)           |
 * |                    | |                                | +--------------------------------+
 * |                    | |                                | +--------------------------------+
 * |                    | +--------------------------------+ |           ev(20, 40)           |
 * |                    |                                    |                                |
 * |                    |                                    |                                |
 * |                    |                                    |                                |
 * |                    |                                    +--------------------------------+
 * |                    | +--------------------+ +--------------------+ +---------------------+
 * |                    | |     ev(40, 60)     | |     ev(40, 60)     | |      ev(40, 60)     |
 * |                    | |                    | |                    | |                     |
 * |                    | |                    | |                    | |                     |
 * |                    | |                    | |                    | |                     |
 * |                    | +--------------------+ +--------------------+ +---------------------+
 * |                    |
 * +--------------------+
 */
internal val ExpandSubGroupOfEvents = listOfEvents(
    Ev(0, 200),
    Ev(10, 30),
    Ev(15, 20),
    Ev(20, 40),
    Ev(40, 60),
    Ev(40, 60),
    Ev(40, 60),
)

/*
 * A long event collides with two subgroups.
 * One subgroup has 4 events, forcing events to take up 1/5th of the width.
 * The other subgroup 3 columns, so events should expand to 4/5 * 1/3.
 * Additionally, one event of this subgroup should expand to double of that size.
 *
 * +----------------+
 * |     ev(0, 90)  |
 * |                | +--------------------+
 * |                | |     ev(10, 35)     | +----------------------------------------------+
 * |                | |                    | |                  ev(15, 20)                  |
 * |                | |                    | +----------------------------------------------+
 * |                | |                    | +---------------------+
 * |                | +--------------------+ |     ev(20, 40)      | +----------------------+
 * |                |                        |                     | |      ev(30, 50)      |
 * |                |                        |                     | |                      |
 * |                |                        |                     | |                      |
 * |                |                        +---------------------+ |                      |
 * |                |                                                +----------------------+
 * |                | +---------------+ +---------------+ +---------------+ +---------------+
 * |                | |   ev(50, 70)  | |   ev(50, 70)  | |   ev(50, 70)  | |   ev(50, 70)  |
 * |                | |               | |               | |               | |               |
 * |                | |               | |               | |               | |               |
 * |                | |               | |               | |               | |               |
 * |                | +---------------+ +---------------+ +---------------+ +---------------+
 * +----------------+
 */
internal val ExpandSubGroupOfEventsOneItemExpandsMore = listOfEvents(
    Ev(0, 90),
    Ev(10, 35),
    Ev(15, 20),
    Ev(20, 40),
    Ev(30, 50),
    Ev(50, 70),
    Ev(50, 70),
    Ev(50, 70),
    Ev(50, 70),
)

/*
 * A long event collides with two subgroups.
 * One subgroup has a long chain of equal events.
 * The other subgroup has 2 columns, one with two events, so events should expand equally
 *
 * +--------------+
 * |   ev(0, 90)  |
 * |              | +----------------------------------+
 * |              | |            ev(10, 30)            | +----------------------------------------+
 * |              | |                                  | |               ev(15, 20)               |
 * |              | |                                  | +----------------------------------------+
 * |              | |                                  | +----------------------------------------+
 * |              | +----------------------------------+ |               ev(20, 40)               |
 * |              |                                      |                                        |
 * |              |                                      |                                        |
 * |              |                                      |                                        |
 * |              | +-------------+                      +----------------------------------------+
 * |              | | ev(30, 50)  | +-------------+ +-------------+ +-------------+ +-------------+
 * |              | |             | | ev(40, 60)  | | ev(40, 60)  | | ev(40, 60)  | | ev(40, 60)  |
 * |              | |             | |             | |             | |             | |             |
 * |              | |             | |             | |             | |             | |             |
 * |              | +-------------+ |             | |             | |             | |             |
 * |              |                 +-------------+ +-------------+ +-------------+ +-------------+
 * |              |
 * +--------------+
 */
internal val RelatedPathsCanExpand = listOfEvents(
    Ev(0, 90),
    Ev(10, 30),
    Ev(15, 20),
    Ev(20, 40),
    Ev(30, 50),
    Ev(40, 60),
    Ev(40, 60),
    Ev(40, 60),
    Ev(40, 60),
)

/*
 * The 4th event expands until it reaches the 3rd event.
 *
 * +-----------------------------+ +-----------------------------+
 * |          ev(10, 30)         | |         ev(10, 30)          |
 * |                             | |                             |
 * |                             | |                             | +------------------------------+
 * |                             | |                             | |          ev(20, 60)          |
 * +-----------------------------+ +-----------------------------+ |                              |
 * +-------------------------------------------------------------+ |                              |
 * |                         ev(30, 60)                          | |                              |
 * |                                                             | |                              |
 * |                                                             | |                              |
 * |                                                             | |                              |
 * +-------------------------------------------------------------+ +------------------------------+
 */
internal val ItemExpandsUntilLastEventOfChain = listOfEvents(
    Ev(10, 30),
    Ev(10, 30),
    Ev(20, 60),
    Ev(30, 60),
)

/*
 * A pair of events can expand until they collide with the last event of a chain
 *
 * +--------------------+
 * |     ev(10, 40)     |
 * |                    | +--------------------+ +--------------------+
 * +--------------------+ |     ev(30, 70)     | |     ev(30, 70)     | +--------------------+
 *                        |                    | |                    | |     ev(35, 120)    |
 *                        |                    | |                    | |                    |
 *                        |                    | |                    | |                    |
 *                        |                    | |                    | |                    |
 *                        +--------------------+ +--------------------+ |                    |
 * +-------------------------------+ +--------------------------------+ |                    |
 * |          ev(70, 110)          | |           ev(70, 110)          | |                    |
 * |                               | |                                | |                    |
 * |                               | |                                | |                    |
 * |                               | |                                | |                    |
 * +-------------------------------+ +--------------------------------+ |                    |
 *                                                                      +--------------------+
 *
 */
internal val SeparatePairExpandsUntilCollision = listOfEvents(
    Ev(10, 40),
    Ev(30, 70),
    Ev(30, 70),
    Ev(35, 120),
    Ev(70, 110),
    Ev(70, 110),
)

/*
 * In subgroup of colliding events, only one can expand because others would collide if expanded.
 *
 * This casi is especially interesting because a better solution is possible:
 *
 * If event 4 is placed in 3rd column and event 3 is placed in 4th column, event 5 can expand more.
 * However, supporting this case is complex and computationally more expensive.
 *
 * +--------------------+
 * |     ev(10, 90)     |
 * |                    | +--------------------+
 * |                    | |     ev(15, 30)     |
 * |                    | |                    | +--------------------+
 * |                    | |                    | |     ev(20, 60)     | +--------------------+
 * |                    | +--------------------+ |                    | |     ev(25, 40)     |
 * |                    |                        |                    | +--------------------+
 * |                    | +--------------------+ |                    |
 * |                    | |     ev(40, 70)     | |                    |
 * |                    | |                    | +--------------------+
 * |                    | |                    | +-------------------------------------------+
 * |                    | |                    | |                ev(60, 80)                 |
 * |                    | +--------------------+ |                                           |
 * |                    |                        |                                           |
 * |                    |                        +-------------------------------------------+
 * +--------------------+
 */
internal val ChainCollidesOnlyOneItemExpands = listOfEvents(
    Ev(10, 90),
    Ev(15, 30),
    Ev(20, 60),
    Ev(25, 40),
    Ev(40, 70),
    Ev(60, 80),
)

/*
 * A subgroup of 4 events can expand.
 * Because of how they are placed, al algorithm might expand them unequally, and they should all
 * have the same width.
 *
 * +----------------+
 * |   ev(10, 90)   |
 * |                | +----------------------+
 * |                | |      ev(15, 50)      | +----------------------+
 * |                | |                      | |      ev(20, 30)      | +----------------------+
 * |                | |                      | |                      | |      ev(25, 40)      |
 * |                | |                      | +----------------------+ |                      |
 * |                | |                      | +----------------------+ |                      |
 * |                | |                      | |      ev(30, 50)      | +----------------------+
 * |                | |                      | |                      |
 * |                | +----------------------+ +----------------------+
 * |                |
 * |                | +----------------+ +----------------+ +----------------+ +----------------+
 * |                | |  ev(60, 80)    | |  ev(60, 80)    | |  ev(60, 80)    | |  ev(60, 80)    |
 * |                | |                | |                | |                | |                |
 * |                | |                | |                | |                | |                |
 * |                | +----------------+ +----------------+ +----------------+ +----------------+
 * |                |
 * |                |
 * +----------------+
 */
internal val EventsWithTwoPathsExpandEqually = listOfEvents(
    Ev(10, 90),
    Ev(15, 50),
    Ev(20, 30),
    Ev(25, 40),
    Ev(30, 50),
    Ev(60, 80),
    Ev(60, 80),
    Ev(60, 80),
    Ev(60, 80),
)

// TODO revisar si el canviem per un altre? o si és nomes per testejar subgraphs
internal val ComplexExpansionSecondEventExpandsButNotUntilEnd = listOfEvents(
    Ev(10, 50),
    Ev(15, 30),
    Ev(20, 50),
    Ev(30, 50),
    Ev(40, 60),
).mapIndexed { index, event -> ('a' + index).toString() to event }.toMap()

internal fun allEventSets() = craftedEventSets() + randomEventSets().map { "Random" to it }

internal fun craftedEventSets(): List<Pair<String, List<TestEvent>>> = listOf(
    ::TwoEventsOneEmpty,
    ::NoOverlapTouchingEvents,
    ::ThreeFullOverlapEvents,
    ::ChainOfThreeButTwoColumns,
    ::ThreeColumnsTwoColumns,
    ::OneBigContainsThreeAndTwoThatShouldExpand,
    ::ExpandSubGroupOfEvents,
    ::ExpandSubGroupOfEventsOneItemExpandsMore,
    ::RelatedPathsCanExpand,
    ::ItemExpandsUntilLastEventOfChain,
    ::ChainCollidesOnlyOneItemExpands,
    ::EventsWithTwoPathsExpandEqually,
    ::SeparatePairExpandsUntilCollision,
).map { ref -> ref.name to ref.get() }

private fun randomEventSets(): List<List<TestEvent>> {
    val random = kotlin.random.Random(SEED)

    return (1..RANDOM_EVENTS_COUNT).map { index ->
        buildList {
            val start = random.nextInt(0, DAY_IN_MINUTES - 1)
            val end = start + random.nextInt(1, DAY_IN_MINUTES - start)
            add(TestEvent(index, start, end))
        }
    }
}

private class Ev(val startMin: Int, val endMin: Int)

private fun listOfEvents(vararg events: Ev) = events.mapIndexed { index, event ->
    TestEvent(index, event.startMin, event.endMin)
}

/*private fun ev(startMin: Int, endMin: Int) = TestEvent(
    startTime = LocalTime(hour = startMin / 60, minute = startMin % 60),
    endTime = LocalTime(hour = endMin / 60, minute = endMin % 60),
)*/

private const val SEED = 12345
private const val DAY_IN_MINUTES = 24 * 60
private const val RANDOM_EVENTS_COUNT = 15
