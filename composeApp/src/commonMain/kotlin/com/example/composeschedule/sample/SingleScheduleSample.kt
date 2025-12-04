package com.example.composeschedule.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composeschedule.CreateScreen
import com.example.schedule.schedule.SingleSchedule
import com.example.schedule.uiConfig.CurrentTimeIndicator
import com.example.schedule.uiConfig.ScheduleLayoutParams
import com.example.schedule.uiConfig.ScheduleTimeBlockDuration
import com.example.schedule.uiConfig.ScheduleTimeBlocks
import com.example.schedule.uiConfig.SingleScheduleConfig
import com.example.schedule.uiConfig.SingleScheduleTheme
import com.example.schedule.uiConfig.TimeBlockDividers
import com.example.schedule.uiConfig.TimeBlockLabels
import com.example.schedule.uiConfig.TimeBlockStyle
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalTime

@Composable
fun SingleScheduleSample(modifier: Modifier = Modifier) {
    MaterialTheme {
        var isCreatingEvent by remember { mutableStateOf(false) }
        var events by remember { mutableStateOf(mockEvents2()) }

        var timeBlockDuration by remember { mutableStateOf(ScheduleTimeBlockDuration.Hour) }

        Box(
            modifier = modifier
                .background(Color.White)
                .padding(vertical = 64.dp)
                .fillMaxSize()
        ) {
            SingleSchedule(
                events = events,
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
                        backgroundColor = Color.White,
                        currentTimeIndicator = CurrentTimeIndicator.Visible(
                            color = Color.Red,
                        ),
                        eventsMinHeight = 24.dp,
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
                    TimeBlockStyle(Color(0xFFF5F5F5), shape, PaddingValues(bottom = 2.dp))
                },
            )

            FloatingActionButton(
                onClick = { isCreatingEvent = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 24.dp, bottom = 48.dp),
            ) {
                Icon(Icons.Filled.Add, null)
            }

            if (isCreatingEvent) {
                CreateScreen(onCreateEvent = { event ->
                    events = (events + event).toPersistentList()
                    isCreatingEvent = false
                })
            }
        }
    }
}

internal val BLOCK_RADIUS = 8.dp
internal val BLOCK_LARGE_RADIUS = 16.dp
