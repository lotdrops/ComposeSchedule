package com.example.composeschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composeschedule.sample.AppEvent
import kotlinx.datetime.LocalTime
import kotlin.random.Random

@Composable
fun CreateScreen(onCreateEvent: (AppEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().background(Color.White)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        var showStartDialog by remember { mutableStateOf(false) }

        var title by remember { mutableStateOf("") }
        TextField(title, onValueChange = { title = it })

        var time by remember { mutableStateOf(LocalTime(0, 0)) }
        Text(
            text = "Selected start time:$time",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = { showStartDialog = true }) {
            Text("Select start Time")
        }

        if (showStartDialog) {
            TimePickerDialog(
                time = time,
                onTimeSelect = {
                    time = it
                    showStartDialog = false
                },
                onDismissRequest = { showStartDialog = false },
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onCreateEvent(
                    AppEvent(
                        Random.nextInt(),
                        title,
                        time,
                        LocalTime(time.hour + 1, time.minute),
                    ),
                )
            },
        ) {
            Text("Create event")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    time: LocalTime,
    onTimeSelect: (LocalTime) -> Unit,
    onDismissRequest: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                val timeState = rememberTimePickerState(time.hour, time.minute)
                TimePicker(state = timeState)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            onDismissRequest()

                            val selectedLocalTime = LocalTime(timeState.hour, timeState.minute)
                            onTimeSelect(selectedLocalTime)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
