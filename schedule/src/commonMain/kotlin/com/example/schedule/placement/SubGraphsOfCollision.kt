package com.example.schedule.placement

import com.example.schedule.ScheduleEvent

/**
 * Given a set of event Columns, [buildSubGraphsOfCollision] finds all paths of events that collide.
 *
 * For example, if the first event of the first column collides with the two first events of the
 * second column, then we compare those two events with the third column, and repeat for all
 * remaining events and columns.
 *
 * **Note** that we do not check for events that skip a column. The columns guarantee that an event
 * is placed to the right if there is a path of collision until the first column.
 *
 * @return a list of lists of events, where each list is a path of events that collide
 */
internal fun <T : ScheduleEvent> List<Column<T>>.buildSubGraphsOfCollision() =
    (this.firstOrNull()?.elements ?: emptyList())
        .map { event ->
            listOf(event).buildSubGraphsOfCollision(this.drop(1))
        }.sumLists()

private fun <T : ScheduleEvent> List<T>.buildSubGraphsOfCollision(
    columnsToTheRight: List<Column<T>>,
): List<List<T>> {
    if (columnsToTheRight.isEmpty()) return listOf(this)

    val colliding = columnsToTheRight[0].elements.filter { event -> last().collidesWith(event) }
    return if (colliding.isEmpty()) {
        listOf(this)
    } else {
        colliding
            .map {
                (this + it).buildSubGraphsOfCollision(columnsToTheRight.drop(1))
            }.sumLists()
    }

    /*he de fer "all that collide"
    Si cap, complet, sinó, seguir
    return columnsToTheRight[0].elements.map { event ->
        println("subgraphs, currentList:$this, event:$event")
        if (last().collidesWith(event)) {
            (this + event)
        } else {
            this
        }.buildSubGraphsOfCollision(columnsToTheRight.drop(1))
    }.sumLists()*/
}

private fun <T : ScheduleEvent> List<List<List<T>>>.sumLists(): List<List<T>> =
    fold(emptyList<List<T>>()) { acc, list -> acc + list }
