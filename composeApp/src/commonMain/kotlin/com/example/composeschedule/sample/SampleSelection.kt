package com.example.composeschedule.sample

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SampleSelection(
    onSingleScheduleClick: () -> Unit,
    onSchedulesListClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(16.dp), verticalArrangement = spacedBy(16.dp)) {
        Button(onSingleScheduleClick) {
            Text("Single Schedule")
        }
        Button(onSchedulesListClick) {
            Text("Schedules List")
        }
    }
}
