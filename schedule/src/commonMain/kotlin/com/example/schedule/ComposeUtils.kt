package com.example.schedule

import androidx.compose.ui.Modifier

inline fun <T : Any> Modifier.modifyIfNotNull(
    value: T?,
    block: Modifier.(T) -> Modifier,
): Modifier = if (value != null) {
    block(value)
} else {
    this
}
