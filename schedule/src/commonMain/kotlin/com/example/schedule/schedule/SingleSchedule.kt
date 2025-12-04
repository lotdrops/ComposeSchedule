package com.example.schedule.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.example.schedule.HOUR_MINUTES
import com.example.schedule.HOUR_SECONDS
import com.example.schedule.MinHeightScheduleEvent
import com.example.schedule.PlacedEvent
import com.example.schedule.ScheduleEvent
import com.example.schedule.modifyIfNotNull
import com.example.schedule.placement.placeEvents
import com.example.schedule.uiConfig.CurrentTimeIndicator
import com.example.schedule.uiConfig.LabelsToEventsDivider
import com.example.schedule.uiConfig.ScheduleTimeBlockDuration
import com.example.schedule.uiConfig.SingleScheduleConfig
import com.example.schedule.uiConfig.TimeBlockDividers
import com.example.schedule.uiConfig.TimeBlockHeight
import com.example.schedule.uiConfig.TimeBlockStyle
import com.example.schedule.uiConfig.TimeLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

internal typealias TimeLabelLambda =
    @Composable (
        time: LocalTime,
        textColor: Color,
        textStyle: TextStyle,
        Modifier,
        timeLabelAlignmentLine: HorizontalAlignmentLine,
        isAmericanFormat: Boolean,
    ) -> Unit

internal typealias TimeIndicatorLambda = @Composable (color: Color, height: Dp, Modifier) -> Unit

private val TimeLabelAlignmentLine = HorizontalAlignmentLine(merger = { old, new -> min(old, new) })

@Composable
fun <T : ScheduleEvent> SingleSchedule(
    events: ImmutableList<T>,
    eventContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    config: SingleScheduleConfig = SingleScheduleConfig(),
    timeLabel: TimeLabelLambda = {
        time,
        textColor,
        textStyle,
        modifier,
        timeLabelAlignmentLine,
        isAmericanFormat,
        ->
        TimeLabel(
            time = time,
            textColor = textColor,
            textStyle = textStyle,
            timeLabelAlignmentLine = timeLabelAlignmentLine,
            isAmericanFormat = isAmericanFormat,
            modifier = modifier,
        )
    },
    currentTimeIndicator: TimeIndicatorLambda = { color, height, modifier ->
        CurrentTimeIndicator(color, height, modifier)
    },
    timeBlockStyleProvider: ((index: Int, count: Int) -> TimeBlockStyle)? = null,
    onTimeBlockClick: ((LocalTime) -> Unit)? = null,
) {
    var minimumHeightInMinutes: Int? by remember {
        mutableStateOf(if (config.layoutParams.eventsMinHeight == 0.dp) 0 else null)
    }
    val setMinHeightInMinutes = { minutes: Int ->
        if (minimumHeightInMinutes != minutes) {
            minimumHeightInMinutes = minutes
        }
    }

    val placedEvents = remember(events, minimumHeightInMinutes) {
        minimumHeightInMinutes?.let { minutes ->
            events.map { MinHeightScheduleEvent(minutes, it) }.placeEvents().toPersistentList()
        } ?: persistentListOf()
    }
    val timeBlockDividerColor = (config.timeBlocks.dividers as? TimeBlockDividers.Shown)?.color
        ?: Color.Transparent
    val verticalDividerColor =
        (config.layoutParams.labelsToEventsDivider as? LabelsToEventsDivider.Shown)?.color
            ?: Color.Transparent

    Layout(
        modifier = when (config.timeBlocks.height) {
            TimeBlockHeight.FillAvailableHeight -> modifier
            is TimeBlockHeight.FixedWithScroll ->
                modifier
                    .verticalScroll(
                        config.timeBlocks.height.scrollState,
                    ).padding(config.timeBlocks.height.contentPadding)
        }.background(config.layoutParams.backgroundColor),
        content = {
            for (index in config.timeBlocks.dividersRange) {
                HorizontalDivider(
                    Modifier.layoutId(LayoutIds.Divider),
                    color = timeBlockDividerColor,
                    thickness = (config.timeBlocks.dividers as? TimeBlockDividers.Shown)?.thickness
                        ?: 0.dp,
                )
            }
            for (index in config.timeBlocks.labelsRange) {
                timeLabel(
                    config.timeBlocks.timeBlockTime(index),
                    config.timeBlocks.labels.textColor,
                    config.timeBlocks.labels.timeLabelTextStyle,
                    Modifier.layoutId(LayoutIds.TimeLabel),
                    TimeLabelAlignmentLine,
                    config.timeBlocks.labels.timeLabelIsAmerican,
                )
            }

            if (config.layoutParams.labelsToEventsDivider is LabelsToEventsDivider.Shown) {
                VerticalDivider(
                    Modifier.layoutId(LayoutIds.VerticalDivider),
                    color = verticalDividerColor,
                    thickness = config.layoutParams.labelsToEventsDivider.thickness,
                )
            }
            if (onTimeBlockClick != null || timeBlockStyleProvider != null) {
                for (timeBlockIndex in 0..<config.timeBlocks.timeBlocksCount) {
                    val timeBlockStartTime = config.timeBlocks.timeBlockTime(timeBlockIndex)

                    Slot(
                        style = timeBlockStyleProvider?.invoke(
                            timeBlockIndex,
                            config.timeBlocks.timeBlocksCount,
                        ),
                        onClick = onTimeBlockClick?.let { { it(timeBlockStartTime) } },
                        Modifier.layoutId(LayoutIds.Slot),
                    )
                }
            }
            placedEvents.forEach { placedEvent ->
                Box(Modifier.layoutId(LayoutIds.Event(placedEvent))) {
                    eventContent(placedEvent.event.event)
                }
            }
            if (config.layoutParams.currentTimeIndicator is CurrentTimeIndicator.Visible) {
                currentTimeIndicator(
                    config.layoutParams.currentTimeIndicator.color,
                    config.layoutParams.currentTimeIndicator.height,
                    Modifier.layoutId(LayoutIds.CurrentTime),
                )
            }
        },
        measurePolicy = singleScheduleMeasurePolicy(
            config = config,
            setMinHeightInMinutes = setMinHeightInMinutes,
        ),
    )
}

@Composable
private fun Slot(
    style: TimeBlockStyle?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .modifyIfNotNull(style) { style ->
                padding(style.paddings)
                    .clip(style.shape)
                    .background(style.backgroundColor)
            }.modifyIfNotNull(onClick) { clickable(onClick = it) },
    )
}

@Suppress("LongMethod")
@Composable
private fun singleScheduleMeasurePolicy(
    config: SingleScheduleConfig,
    setMinHeightInMinutes: (Int) -> Unit,
): MeasurePolicy {
    val eventsMinHeightPx =
        with(LocalDensity.current) {
            config.layoutParams.eventsMinHeight
                .toPx()
                .roundToInt()
        }

    val timeBlockHeightPx = with(LocalDensity.current) {
        (config.timeBlocks.height as? TimeBlockHeight.FixedWithScroll)?.height?.toPx()?.roundToInt()
    }
    val timeIndicatorHeight = with(LocalDensity.current) {
        (config.layoutParams.currentTimeIndicator as? CurrentTimeIndicator.Visible)
            ?.height
            ?.toPx()
            ?.roundToInt()
    }
    val timeIndicatorOffsetX = -(timeIndicatorHeight ?: 0) / 2
    val startMargin = config.timeBlocks.labels.timeLabelStartPadding
        .toPx()
    val timeBlocksDividerThickness =
        (config.timeBlocks.dividers as? TimeBlockDividers.Shown)?.thickness?.toPx() ?: 0
    val verticalDividerThickness =
        (config.layoutParams.labelsToEventsDivider as? LabelsToEventsDivider.Shown)
            ?.thickness
            ?.toPx() ?: 0
    val eventEndMargin = config.layoutParams.eventsEndMargin.toPx()
    val labelsEndMargin = config.timeBlocks.labels.timeLabelEndPadding
        .toPx()

    return remember(config) {
        MeasurePolicy { measurables, constraints ->
            val showFirstTimeLabel = config.timeBlocks.labels.showFirstTimeLabel
            val showLastTimeLabel = config.timeBlocks.labels.showLastTimeLabel
            val showFirstDivider =
                (config.timeBlocks.dividers as? TimeBlockDividers.Shown)?.showFirstDivider == true
            val wrapConstraints = constraints.copy(minWidth = 0, minHeight = 0)

            val timeLabelPlaceables = measurables
                .filter { it.layoutId == LayoutIds.TimeLabel }
                .fastMap { it.measure(wrapConstraints) }
            val timeLabelAlignmentLines: List<Int?> = timeLabelPlaceables.fastMap { label ->
                label[TimeLabelAlignmentLine].takeIf { it != AlignmentLine.Unspecified }
            }
            val firstLabelOffset = if (showFirstTimeLabel) {
                timeLabelPlaceables.first().height / 2
            } else {
                0
            }
            val lastLabelExcess = if (showLastTimeLabel) {
                timeLabelPlaceables.last().height / 2
            } else {
                0
            }
            val labelsExcess = firstLabelOffset + lastLabelExcess
            val timeBlockHeightPx = timeBlockHeightPx
                ?: ((constraints.maxHeight - labelsExcess) / config.timeBlocks.timeBlocksCount)
            val totalHeight = timeBlockHeightPx * config.timeBlocks.timeBlocksCount +
                firstLabelOffset + lastLabelExcess

            val pixelsToMinutes = config.timeBlocks.duration.minutes / timeBlockHeightPx.toFloat()
            setMinHeightInMinutes((eventsMinHeightPx * pixelsToMinutes).roundToInt())

            val labelsEnd = timeLabelPlaceables.maxOf { it.width } + startMargin
            val startOfDividers = labelsEnd + labelsEndMargin
            val startOfEvents = startOfDividers
            val eventsAvailableWidth = constraints.maxWidth - (startOfEvents + eventEndMargin)

            val dividerPlaceables = measurables
                .filter { it.layoutId == LayoutIds.Divider }
                .fastMap {
                    it.measure(
                        Constraints.fixed(
                            width = constraints.maxWidth - startOfDividers,
                            height = timeBlocksDividerThickness,
                        ),
                    )
                }
            val currentTimePlaceable = measurables
                .filter { it.layoutId == LayoutIds.CurrentTime }
                .fastMap {
                    it.measure(
                        Constraints.fixed(
                            width = constraints.maxWidth - startOfDividers - timeIndicatorOffsetX,
                            height = timeBlockHeightPx,
                        ),
                    )
                }
            val slotPlaceables = measurables
                .filter { it.layoutId == LayoutIds.Slot }
                .map {
                    it.measure(
                        Constraints.fixed(
                            width = constraints.maxWidth - startOfEvents,
                            height = timeBlockHeightPx,
                        ),
                    )
                }

            val verticalDividerPlaceable = measurables
                .filter { it.layoutId == LayoutIds.VerticalDivider }
                .map {
                    it.measure(
                        Constraints.fixed(
                            width = verticalDividerThickness,
                            height = totalHeight,
                        ),
                    )
                }

            val eventPlaceablesWithPlacement = measurables
                .filter { it.layoutId is LayoutIds.Event<*> }
                .map {
                    val placedEvent = (it.layoutId as LayoutIds.Event<*>).placedEvent
                    it.measure(
                        Constraints.fixed(
                            width = placedEvent.getWidth(
                                availableWidth = eventsAvailableWidth,
                            ),
                            height = placedEvent.getHeight(
                                timeBlockHeightPx = timeBlockHeightPx,
                                timeBlockDuration = config.timeBlocks.duration,
                            ),
                        ),
                    ) to placedEvent
                }

            layout(constraints.maxWidth, totalHeight) {
                timeLabelPlaceables.forEachIndexed { index, placeable ->
                    val alignmentLine = timeLabelAlignmentLines[index]
                    val yOffset = if (alignmentLine != null) {
                        -alignmentLine
                    } else {
                        -placeable.height / 2
                    }
                    val x = labelsEnd - placeable.width
                    val correctedIndex = if (showFirstTimeLabel) index else index + 1
                    val y = (correctedIndex) * timeBlockHeightPx + yOffset + firstLabelOffset
                    placeable.placeRelative(x, y)
                }
                slotPlaceables.forEachIndexed { index, placeable ->
                    placeable.placeRelative(
                        x = startOfEvents,
                        y = index * timeBlockHeightPx + firstLabelOffset,
                    )
                }
                dividerPlaceables.forEachIndexed { index, placeable ->
                    val correctedIndex = if (showFirstDivider) index else index + 1
                    placeable.placeRelative(
                        x = startOfDividers,
                        y = (correctedIndex) * timeBlockHeightPx + firstLabelOffset,
                    )
                }
                verticalDividerPlaceable
                    .firstOrNull()
                    ?.placeRelative(startOfEvents, firstLabelOffset)
                eventPlaceablesWithPlacement.forEach { (placeable, placed) ->
                    val x = with(placed) {
                        startOfEvents + eventsAvailableWidth * x.value
                    }.roundToInt()
                    placeable.placeRelative(
                        x,
                        y = placed.event.startTime.toScheduleY(
                            timeBlockHeightPx,
                            config.timeBlocks.duration,
                            config.timeBlocks.scheduleStartTime,
                        ) + firstLabelOffset,
                    )
                }
                currentTimePlaceable.firstOrNull()?.let { placeable ->
                    val timeZone =
                        (config.layoutParams.currentTimeIndicator as? CurrentTimeIndicator.Visible)
                            ?.timeZone ?: TimeZone.currentSystemDefault()
                    val y = yPixelsToCurrentTime(
                        timeBlockHeightPx,
                        config.timeBlocks.duration,
                        config.timeBlocks.scheduleStartTime,
                        timeZone,
                    ) - timeBlockHeightPx / 2 + firstLabelOffset
                    placeable.placeRelative(startOfDividers + timeIndicatorOffsetX, y)
                }
            }
        }
    }
}

private fun PlacedEvent<*>.getHeight(
    timeBlockHeightPx: Int,
    timeBlockDuration: ScheduleTimeBlockDuration,
) = (event.endTime.toSecondOfDay() - event.startTime.toSecondOfDay())
    .secondsToHeight(timeBlockHeightPx, timeBlockDuration)

private fun PlacedEvent<*>.getWidth(availableWidth: Int) =
    (availableWidth * width.value).roundToInt()

private fun LocalTime.toScheduleY(
    timeBlockHeightPx: Int,
    timeBlockDuration: ScheduleTimeBlockDuration,
    scheduleStartTime: LocalTime,
): Int = (toSecondOfDay() - scheduleStartTime.toSecondOfDay()).secondsToHeight(
    timeBlockHeightPx,
    timeBlockDuration,
)

private fun Int.secondsToHeight(
    timeBlockHeightPx: Int,
    timeBlockDuration: ScheduleTimeBlockDuration,
): Int = (
    this / HOUR_SECONDS.toFloat() * timeBlockHeightPx * HOUR_MINUTES /
        timeBlockDuration.minutes
).roundToInt()

@Composable
private fun Dp.toPx(): Int = with(LocalDensity.current) {
    this@toPx.toPx().roundToInt()
}

private sealed class LayoutIds {
    data object Divider : LayoutIds()

    data object VerticalDivider : LayoutIds()

    data object TimeLabel : LayoutIds()

    data object Slot : LayoutIds()

    data object CurrentTime : LayoutIds()

    data class Event<T : ScheduleEvent>(val placedEvent: PlacedEvent<T>) : LayoutIds()
}

@OptIn(ExperimentalTime::class)
fun yPixelsToCurrentTime(
    timeBlockHeightPx: Int,
    timeBlockDuration: ScheduleTimeBlockDuration,
    scheduleStartTime: LocalTime,
    timeZone: TimeZone,
) = Clock.System.now().toLocalDateTime(timeZone).time.toScheduleY(
    timeBlockHeightPx,
    timeBlockDuration,
    scheduleStartTime,
)
