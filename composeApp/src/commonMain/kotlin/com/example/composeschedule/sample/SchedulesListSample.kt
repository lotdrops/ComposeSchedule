package com.example.composeschedule.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.schedule.schedule.ScheduleDaysPager
import com.example.schedule.schedule.SchedulesPagerHeader
import com.example.schedule.uiConfig.CurrentTimeIndicator
import com.example.schedule.uiConfig.LabelsToEventsDivider
import com.example.schedule.uiConfig.ScheduleLayoutParams
import com.example.schedule.uiConfig.ScheduleLayoutParams.Companion.invoke
import com.example.schedule.uiConfig.ScheduleTimeBlockDuration
import com.example.schedule.uiConfig.ScheduleTimeBlocks
import com.example.schedule.uiConfig.ScheduleTimeBlocks.Companion.invoke
import com.example.schedule.uiConfig.SingleScheduleConfig
import com.example.schedule.uiConfig.SingleScheduleTheme
import com.example.schedule.uiConfig.TimeBlockDividers
import com.example.schedule.uiConfig.TimeBlockLabels
import com.example.schedule.uiConfig.TimeBlockLabels.Companion.invoke
import com.example.schedule.uiConfig.TimeBlockStyle
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun SchedulesListSample() {
    var timeBlockDuration by remember { mutableStateOf(ScheduleTimeBlockDuration.Hour) }

    val events by remember {
        mutableStateOf(
            persistentListOf(
                DummyDaySchedule(
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
                    mockEvents1(),
                ),
                DummyDaySchedule(
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(DatePeriod(days = 1)),
                    mockEvents2(),
                ),
            )

        )
    }
    ScheduleDaysPager(
        days = events,
        pagesHeader = { PagesHeader(events, it) },
        eventContent = { EventContent(it) },
        config = SingleScheduleConfig(
            timeBlocks = ScheduleTimeBlocks(
                scheduleStartTime = LocalTime(8, 0),
                scheduleEndTime = LocalTime(16, 0),
                duration = timeBlockDuration,
                dividers = TimeBlockDividers.Hidden,
                labels = TimeBlockLabels(
                    timeLabelTextStyle = MaterialTheme.typography.bodyMedium,
                    showFirstTimeLabel = true,
                    showLastTimeLabel = true,
                ),
            ),
            layoutParams = ScheduleLayoutParams(
                backgroundColor = Color(0xFFF7F7F7),
                currentTimeIndicator = CurrentTimeIndicator.Visible(
                    color = Color.Red,
                ),
                labelsToEventsDivider = LabelsToEventsDivider.Hidden,
            ),
        ),
        timeBlockStyleProvider = { index, count ->
            val shape = when (index) {
                0 -> RoundedCornerShape(
                    topStart = BLOCK_LARGE_RADIUS,
                    topEnd = BLOCK_RADIUS,
                    bottomStart = BLOCK_RADIUS,
                    bottomEnd = BLOCK_RADIUS,
                )

                count - 1 -> RoundedCornerShape(
                    topStart = BLOCK_RADIUS,
                    topEnd = BLOCK_RADIUS,
                    bottomStart = BLOCK_LARGE_RADIUS,
                    bottomEnd = BLOCK_RADIUS,
                )

                else -> RoundedCornerShape(size = BLOCK_RADIUS)
            }
            TimeBlockStyle(Color.White, shape, PaddingValues(bottom = 2.dp))
        }
    )
}

@Composable
private fun PagesHeader(
    days: PersistentList<DummyDaySchedule<AppEvent>>,
    header: SchedulesPagerHeader<AppEvent, DummyDaySchedule<AppEvent>>,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { index, page ->
            val color = if (days[index] == header.page) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }

            Button(
                onClick = { header.onIndexSelected(index) },
                Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors().copy(containerColor = color),
            ) {
                Text(page.day.toString())
            }
        }
    }
}
