package com.example.schedule.uiConfig

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.schedule.DAY_SECONDS
import com.example.schedule.MINUTE_SECONDS
import com.example.schedule.plusMinutes
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Config of a single (just one column) Schedule.
 *
 * A time block is the slot between time labels, it represents how the schedule is divided
 * vertically.
 *
 * @param [timeBlocks]      configures parameters for time blocks: labels, dividers, size
 *                          visible schedule range, size, block duration
 * @param [layoutParams]    configures parameters related to the whole layout: background color,
 *                          time indicator, divider between labels and events, end margin,
 *                          minimum height for events
 */
@Immutable
@ExposedCopyVisibility
data class SingleScheduleConfig private constructor(
    val timeBlocks: ScheduleTimeBlocks,
    val layoutParams: ScheduleLayoutParams,
) {
    fun copy(
        timeBlocks: ScheduleTimeBlocks,
    ): SingleScheduleConfig = SingleScheduleConfig(timeBlocks, layoutParams)

    fun copy(
        layoutParams: ScheduleLayoutParams,
    ): SingleScheduleConfig = SingleScheduleConfig(timeBlocks, layoutParams)

    companion object {
        @Composable
        operator fun invoke(
            timeBlocks: ScheduleTimeBlocks = ScheduleTimeBlocks(),
            layoutParams: ScheduleLayoutParams = ScheduleLayoutParams(),
        ) = SingleScheduleConfig(timeBlocks, layoutParams)
    }
}

@ConsistentCopyVisibility
@Immutable
data class ScheduleTimeBlocks private constructor(
    val scheduleStartTime: LocalTime,
    val scheduleEndTime: LocalTime,
    val height: TimeBlockHeight,
    val duration: ScheduleTimeBlockDuration,
    val labels: TimeBlockLabels,
    val dividers: TimeBlockDividers,
) {
    val timeBlocksCount: Int = (
        scheduleEndTime.toTimeBlocksCount(duration) - scheduleStartTime.toTimeBlocksCount(duration)
    ).coerceAtLeast(0)

    internal val dividersRange: IntRange = if (dividers is TimeBlockDividers.Shown) {
        val firstIndex = if (dividers.showFirstDivider) 0 else 1
        val lastIndex = if (dividers.showLastDivider) timeBlocksCount else timeBlocksCount - 1
        firstIndex..lastIndex
    } else {
        0 until 1
    }

    internal val labelsRange: IntRange get() {
        val firstIndex = if (labels.showFirstTimeLabel) 0 else 1
        val lastIndex = if (labels.showLastTimeLabel) timeBlocksCount else timeBlocksCount - 1
        return firstIndex..lastIndex
    }

    internal fun timeBlockTime(
        timeBlockIndex: Int,
    ): LocalTime = scheduleStartTime.plusMinutes(timeBlockIndex * duration.minutes)

    companion object {
        @Composable
        operator fun invoke(
            scheduleStartTime: LocalTime = LocalTime(0, 0),
            scheduleEndTime: LocalTime = LocalTime(hour = 23, minute = 59),
            height: TimeBlockHeight = TimeBlockHeight.FillAvailableHeight,
            duration: ScheduleTimeBlockDuration = ScheduleTimeBlockDuration.Hour,
            labels: TimeBlockLabels = TimeBlockLabels(),
            dividers: TimeBlockDividers = TimeBlockDividers.Shown(),
        ): ScheduleTimeBlocks = ScheduleTimeBlocks(
            scheduleStartTime = scheduleStartTime.toClosestTimeBlockTime(duration),
            scheduleEndTime = scheduleEndTime.toClosestTimeBlockTime(duration),
            height = height,
            duration = duration,
            labels = labels,
            dividers = dividers,
        )
    }
}

@Immutable
data class ScheduleLayoutParams(
    val backgroundColor: Color,
    val currentTimeIndicator: CurrentTimeIndicator,
    val labelsToEventsDivider: LabelsToEventsDivider,
    val eventsMinHeight: Dp,
    val eventsEndMargin: Dp = 16.dp,
) {
    companion object {
        @Composable
        operator fun invoke(
            backgroundColor: Color = MaterialTheme.colorScheme.surface,
            currentTimeIndicator: CurrentTimeIndicator = CurrentTimeIndicator.None,
            labelsToEventsDivider: LabelsToEventsDivider = LabelsToEventsDivider.Hidden,
            eventsMinHeight: Dp = 0.dp,
            eventsEndMargin: Dp = 16.dp,
        ) = ScheduleLayoutParams(
            backgroundColor = backgroundColor,
            currentTimeIndicator = currentTimeIndicator,
            labelsToEventsDivider = labelsToEventsDivider,
            eventsMinHeight = eventsMinHeight,
            eventsEndMargin = eventsEndMargin,
        )
    }
}

@Immutable
data class TimeBlockLabels(
    val textColor: Color,
    val timeLabelTextStyle: TextStyle,
    val timeLabelStartPadding: Dp,
    val timeLabelEndPadding: Dp,
    val timeLabelIsAmerican: Boolean,
    val showFirstTimeLabel: Boolean,
    val showLastTimeLabel: Boolean,
) {
    companion object {
        @Composable
        operator fun invoke(
            textColor: Color = MaterialTheme.colorScheme.onSurface,
            timeLabelTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
            timeLabelStartPadding: Dp = 12.dp,
            timeLabelEndPadding: Dp = timeLabelStartPadding,
            timeLabelIsAmerican: Boolean = false,
            showFirstTimeLabel: Boolean = false,
            showLastTimeLabel: Boolean = false,
        ) = TimeBlockLabels(
            textColor = textColor,
            timeLabelTextStyle = timeLabelTextStyle,
            timeLabelStartPadding = timeLabelStartPadding,
            timeLabelEndPadding = timeLabelEndPadding,
            timeLabelIsAmerican = timeLabelIsAmerican,
            showFirstTimeLabel = showFirstTimeLabel,
            showLastTimeLabel = showLastTimeLabel,
        )
    }
}

@Immutable
sealed class TimeBlockDividers {
    data object Hidden : TimeBlockDividers()

    data class Shown(
        val color: Color,
        val thickness: Dp,
        val showFirstDivider: Boolean,
        val showFirstTimeLabel: Boolean,
        val showLastDivider: Boolean,
        val showLastTimeLabel: Boolean,
    ) : TimeBlockDividers() {
        companion object {
            @Composable
            operator fun invoke(
                color: Color = MaterialTheme.colorScheme.outline,
                thickness: Dp = 1.dp,
                showFirstDivider: Boolean = false,
                showFirstTimeLabel: Boolean = false,
                showLastDivider: Boolean = false,
                showLastTimeLabel: Boolean = false,
            ) = Shown(
                color = color,
                thickness = thickness,
                showFirstDivider = showFirstDivider,
                showFirstTimeLabel = showFirstTimeLabel,
                showLastDivider = showLastDivider,
                showLastTimeLabel = showLastTimeLabel,
            )
        }
    }
}

@Immutable
sealed class LabelsToEventsDivider {
    data object Hidden : LabelsToEventsDivider()

    data class Shown(val color: Color, val thickness: Dp) : LabelsToEventsDivider() {
        companion object {
            @Composable
            operator fun invoke(
                color: Color = MaterialTheme.colorScheme.outline,
                thickness: Dp = 1.dp,
            ) = Shown(color, thickness)
        }
    }
}

data class SingleScheduleTheme(
    val backgroundColor: Color,
    val horizontalDividerColor: Color,
    val verticalDividerColor: Color,
    val timeLabelColor: Color,
    val timeLabelTextStyle: TextStyle,
    val currentTimeIndicatorColor: Color,
) {
    companion object {
        @Composable
        operator fun invoke(
            backgroundColor: Color = MaterialTheme.colorScheme.surface,
            horizontalDividerColor: Color = MaterialTheme.colorScheme.outline,
            verticalDividerColor: Color = horizontalDividerColor,
            timeLabelColor: Color = MaterialTheme.colorScheme.onSurface,
            timeLabelTextStyle: TextStyle = MaterialTheme.typography.bodySmall,
            currentTimeIndicatorColor: Color = MaterialTheme.colorScheme.secondary,
        ) = SingleScheduleTheme(
            backgroundColor = backgroundColor,
            horizontalDividerColor = horizontalDividerColor,
            verticalDividerColor = verticalDividerColor,
            timeLabelColor = timeLabelColor,
            timeLabelTextStyle = timeLabelTextStyle,
            currentTimeIndicatorColor = currentTimeIndicatorColor,
        )
    }
}

data class TimeBlockStyle(
    val backgroundColor: Color = Color.Transparent,
    val shape: Shape = RectangleShape,
    val paddings: PaddingValues = PaddingValues(0.dp),
)

@Stable
sealed class TimeBlockHeight {
    data object FillAvailableHeight : TimeBlockHeight()

    data class FixedWithScroll(
        val height: Dp,
        val contentPadding: PaddingValues,
        val scrollState: ScrollState,
    ) : TimeBlockHeight()
}

@Stable
sealed class CurrentTimeIndicator {
    data object None : CurrentTimeIndicator()

    data class Visible(
        val height: Dp = DefaultHeight,
        val color: Color,
        val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ) : CurrentTimeIndicator() {
        companion object {
            @Composable
            operator fun invoke(
                color: Color = MaterialTheme.colorScheme.secondary,
                height: Dp = DefaultHeight,
                timeZone: TimeZone = TimeZone.currentSystemDefault(),
            ): Visible = Visible(color, height, timeZone)
        }
    }

    companion object {
        @OptIn(ExperimentalTime::class)
        internal fun showIfToday(
            day: LocalDate,
            timeZone: TimeZone,
            color: Color,
            height: Dp = DefaultHeight,
        ): CurrentTimeIndicator {
            if (Clock.System
                    .now()
                    .toLocalDateTime(timeZone)
                    .date != day
            ) {
                return None
            }
            return Visible(
                timeZone = timeZone,
                color = color,
                height = height,
            )
        }

        private val DefaultHeight = 12.dp
    }
}

@Immutable
enum class ScheduleTimeBlockDuration {
    QuarterHour,
    HalfHour,
    Hour,
    ;

    val minutes: Int
        get() = when (this) {
            QuarterHour -> 15
            HalfHour -> 30
            Hour -> 60
        }
}

@Suppress("MagicNumber")
@Composable
internal fun TimeLabel(
    time: LocalTime,
    textColor: Color,
    textStyle: TextStyle,
    timeLabelAlignmentLine: HorizontalAlignmentLine,
    isAmericanFormat: Boolean,
    modifier: Modifier = Modifier,
) {
    var firstLineCenterY by remember { mutableIntStateOf(0) }
    val hours = (time.hour % (if (isAmericanFormat) 12 else 24)).toString().padStart(2, '0')
    val minutes = time.minute.toString().padStart(2, '0')
    val suffix = if (isAmericanFormat) "\n${if (time.hour >= 12) "PM" else "AM"}" else ""
    Text(
        text = "$hours:$minutes$suffix",
        style = textStyle,
        color = textColor,
        textAlign = TextAlign.End,
        onTextLayout = { result: TextLayoutResult ->
            if (result.lineCount == 0) return@Text
            val firstLineTop = result.getLineTop(0)
            val firstLineBottom = result.getLineBottom(0)
            firstLineCenterY = ((firstLineTop + firstLineBottom) / 2).roundToInt()
        },
        modifier = modifier.assignAlignmentLine(timeLabelAlignmentLine, firstLineCenterY),
    )
}

private fun Modifier.assignAlignmentLine(
    alignmentLine: HorizontalAlignmentLine,
    firstLineCenterY: Int,
): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(
        placeable.width,
        placeable.height,
        alignmentLines = mapOf(alignmentLine to firstLineCenterY),
    ) {
        placeable.place(0, 0)
    }
}

@Composable
internal fun CurrentTimeIndicator(color: Color, height: Dp, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(height)
                .background(color, shape = CircleShape),
        )
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxSize()
                .background(color),
        )
    }
}

private fun LocalTime.toClosestTimeBlockTime(
    timeBlockDuration: ScheduleTimeBlockDuration,
): LocalTime = LocalTime.fromSecondOfDay(
    (toTimeBlocksCount(timeBlockDuration) * timeBlockDuration.minutes * MINUTE_SECONDS)
        .coerceAtMost(DAY_SECONDS - 1),
)

private fun LocalTime.toTimeBlocksCount(timeBlockDuration: ScheduleTimeBlockDuration): Int =
    (this.toSecondOfDay() / MINUTE_SECONDS / timeBlockDuration.minutes.toFloat()).roundToInt()
