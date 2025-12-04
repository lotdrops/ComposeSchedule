package com.example.composeschedule

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.composeschedule.sample.SampleSelection
import com.example.composeschedule.sample.SchedulesListSample
import com.example.composeschedule.sample.SingleScheduleSample

@Composable
fun App() {
    Box(Modifier.fillMaxSize().safeDrawingPadding()) {
        var selectedPage by remember { mutableStateOf(SamplePage.Selection) }

        when (selectedPage) {
            SamplePage.Selection -> SampleSelection(
                onSingleScheduleClick = { selectedPage = SamplePage.SingleSchedule },
                onSchedulesListClick = { selectedPage = SamplePage.SchedulesList },
            )
            SamplePage.SingleSchedule -> SingleScheduleSample()
            SamplePage.SchedulesList -> SchedulesListSample()
        }
    }
}

private enum class SamplePage {
    Selection,
    SingleSchedule,
    SchedulesList,
}
