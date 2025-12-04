package com.example.schedule.schedule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.schedule.ScheduleEvent
import com.example.schedule.uiConfig.CurrentTimeIndicator
import com.example.schedule.uiConfig.SingleScheduleConfig
import com.example.schedule.uiConfig.TimeBlockStyle
import com.example.schedule.uiConfig.TimeLabel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
fun <E : ScheduleEvent, T : SchedulesDayPage<E>> ScheduleDaysPager(
    days: ImmutableList<T>,
    pagesHeader: @Composable ((SchedulesPagerHeader<E, T>) -> Unit)?,
    eventContent: @Composable (E) -> Unit,
    modifier: Modifier = Modifier,
    config: SingleScheduleConfig = SingleScheduleConfig(),
    showCurrentTimeIndicatorForTodayPage: Boolean = true,
    timeLabel: TimeLabelLambda = {
        time,
        textColor,
        textStyle,
        modifier,
        timeLabelAlignmentLine,
        isAmericanFormat,
        ->
        TimeLabel(time, textColor, textStyle, timeLabelAlignmentLine, isAmericanFormat, modifier)
    },
    currentTimeIndicator: TimeIndicatorLambda = { color, height, modifier ->
        CurrentTimeIndicator(color, height, modifier)
    },
    timeBlockStyleProvider: ((index: Int, count: Int) -> TimeBlockStyle)? = null,
    onTimeBlockClick: ((T, LocalTime) -> Unit)? = null,
) {
    SchedulesPagerImpl(
        pages = days,
        pagesHeader = pagesHeader,
        eventContent = eventContent,
        modifier = modifier,
        config = config,
        showCurrentTimeIndicatorForTodayPage = showCurrentTimeIndicatorForTodayPage,
        timeLabel = timeLabel,
        currentTimeIndicator = currentTimeIndicator,
        timeBlockStyleProvider = timeBlockStyleProvider,
        onTimeBlockClick = onTimeBlockClick,
    )
}

@Composable
fun <E : ScheduleEvent, T : SchedulesPage<E>> SchedulesListPager(
    pages: ImmutableList<T>,
    pagesHeader: @Composable ((SchedulesPagerHeader<E, T>) -> Unit)?,
    eventContent: @Composable (E) -> Unit,
    modifier: Modifier = Modifier,
    config: SingleScheduleConfig = SingleScheduleConfig(),
    showCurrentTimeIndicatorForTodayPage: Boolean = true,
    timeLabel: TimeLabelLambda = {
        time,
        textColor,
        textStyle,
        modifier,
        timeLabelAlignmentLine,
        isAmericanFormat,
        ->
        TimeLabel(time, textColor, textStyle, timeLabelAlignmentLine, isAmericanFormat, modifier)
    },
    currentTimeIndicator: TimeIndicatorLambda = { color, height, modifier ->
        CurrentTimeIndicator(color, height, modifier)
    },
    timeBlockStyleProvider: ((index: Int, count: Int) -> TimeBlockStyle)? = null,
    onTimeBlockClick: ((T, LocalTime) -> Unit)? = null,
) {
    SchedulesPagerImpl(
        pages = pages,
        pagesHeader = pagesHeader,
        eventContent = eventContent,
        modifier = modifier,
        config = config,
        showCurrentTimeIndicatorForTodayPage = showCurrentTimeIndicatorForTodayPage,
        timeLabel = timeLabel,
        currentTimeIndicator = currentTimeIndicator,
        timeBlockStyleProvider = timeBlockStyleProvider,
        onTimeBlockClick = onTimeBlockClick,
    )
}

@Composable
private fun <E : ScheduleEvent, T : SchedulesPage<E>> SchedulesPagerImpl(
    pages: ImmutableList<T>,
    pagesHeader: @Composable ((SchedulesPagerHeader<E, T>) -> Unit)?,
    eventContent: @Composable (E) -> Unit,
    modifier: Modifier = Modifier,
    config: SingleScheduleConfig = SingleScheduleConfig(),
    showCurrentTimeIndicatorForTodayPage: Boolean,
    timeLabel: TimeLabelLambda = {
        time,
        textColor,
        textStyle,
        modifier,
        timeLabelAlignmentLine,
        isAmericanFormat,
        ->
        TimeLabel(time, textColor, textStyle, timeLabelAlignmentLine, isAmericanFormat, modifier)
    },
    currentTimeIndicator: TimeIndicatorLambda = { color, height, modifier ->
        CurrentTimeIndicator(color, height, modifier)
    },
    timeBlockStyleProvider: ((index: Int, count: Int) -> TimeBlockStyle)? = null,
    onTimeBlockClick: ((T, LocalTime) -> Unit)? = null,
) {
    val pagerState = rememberPagerState(0, pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val onIndexSelected: (Int) -> Unit = remember {
        { index -> coroutineScope.launch { pagerState.animateScrollToPage(index) } }
    }

    Column(modifier.fillMaxSize()) {
        val selectedPage = pages.getOrNull(pagerState.currentPage)
        if (pagesHeader != null && selectedPage != null) {
            pagesHeader(SchedulesPagerHeader(selectedPage, onIndexSelected))
        }

        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            SingleSchedule(
                events = pages[page].events,
                eventContent = eventContent,
                config = if (showCurrentTimeIndicatorForTodayPage) {
                    config.configureTimeIndicatorForPage(pages[page])
                } else {
                    config
                },
                timeLabel = timeLabel,
                currentTimeIndicator = currentTimeIndicator,
                timeBlockStyleProvider = timeBlockStyleProvider,
                onTimeBlockClick = onTimeBlockClick?.let { onClick ->
                    { onClick(pages[page], it) }
                },
            )
        }
    }
}

private fun SingleScheduleConfig.configureTimeIndicatorForPage(page: SchedulesPage<*>) =
    if (this.layoutParams.currentTimeIndicator is CurrentTimeIndicator.Visible &&
        page is SchedulesDayPage<*>
    ) {
        this.copy(
            layoutParams = this.layoutParams.copy(
                currentTimeIndicator = CurrentTimeIndicator.showIfToday(
                    page.day,
                    layoutParams.currentTimeIndicator.timeZone,
                    color = layoutParams.currentTimeIndicator.color,
                    height = layoutParams.currentTimeIndicator.height,
                ),
            ),
        )
    } else {
        this
    }

interface SchedulesPage<T : ScheduleEvent> {
    val events: ImmutableList<T>
}

interface SchedulesDayPage<T : ScheduleEvent> : SchedulesPage<T> {
    val day: LocalDate
}

data class SchedulesPagerHeader<E : ScheduleEvent, T : SchedulesPage<E>>(
    val page: T,
    val onIndexSelected: (Int) -> Unit,
)
