package com.example.schedule.placement

import kotlin.test.Test
import kotlin.test.assertEquals

class SubGraphsOfCollisionTest {
    @Test
    fun testBuildSubgraphs() {
        val events = ComplexExpansionSecondEventExpandsButNotUntilEnd
        val getEv = { letter: String -> events[letter]!! }
        val columns = listOf(
            Column(listOf(getEv("a"))),
            Column(listOf(getEv("b"), getEv("d"))),
            Column(listOf(getEv("c"))),
            Column(listOf(getEv("e"))),
        )

        val expected = listOf(
            listOf(getEv("a"), getEv("b"), getEv("c"), getEv("e")),
            listOf(getEv("a"), getEv("d"), getEv("c"), getEv("e")),
        )
        /*val computed = columns[0].elements.flatMap {
            it.buildSubgraphsOfCollision(columns.drop(1))
        }*/
        val computed = columns.buildSubGraphsOfCollision()

        assertEquals(expected.toSet(), computed.toSet())
        assertEquals(expected.size, computed.size)
    }

    // TODO properly test this or delete tests
    @Test
    fun testBuildSubgraphs2() {
        val events = OneBigContainsThreeAndTwoThatShouldExpand
            .mapIndexed { index, event -> index to event }
            .toMap()
        val getEv = { index: Int -> events[index]!! }
        val columns = listOf(
            Column(listOf(getEv(0))),
            Column(listOf(getEv(1), getEv(3))),
            Column(listOf(getEv(2), getEv(4))),
            Column(listOf(getEv(5))),
        )

        val expected = listOf(
            listOf(getEv(0), getEv(1), getEv(2)),
            listOf(getEv(0), getEv(3), getEv(4), getEv(5)),
        )
        /*val computed = columns[0].elements.flatMap {
            it.buildSubgraphsOfCollision(columns.drop(1))
        }*/
        val computed = columns.buildSubGraphsOfCollision()

        println("computed:${computed.joinToString("\n") { it.joinToString { it.id.toString()} }}")

        assertEquals(expected.toSet(), computed.toSet())
        assertEquals(expected.size, computed.size)
    }
}
