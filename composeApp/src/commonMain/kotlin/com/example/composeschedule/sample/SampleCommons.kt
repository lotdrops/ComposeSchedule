package com.example.composeschedule.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.schedule.ScheduleEvent
import com.example.schedule.schedule.SchedulesDayPage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
fun EventContent(event: AppEvent, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.fillMaxSize().padding(end = 2.dp, bottom = 2.dp),
    ) {
        Text(event.title, Modifier.padding(4.dp))
    }
}

@Immutable
data class DummyDaySchedule<T : ScheduleEvent>(
    override val day: LocalDate,
    override val events: ImmutableList<T>,
) : SchedulesDayPage<T>

data class AppEvent(
    val id: Int,
    val title: String,
    override val startTime: LocalTime,
    override val endTime: LocalTime,
) : ScheduleEvent
