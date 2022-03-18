package model.state

import androidx.compose.runtime.Composable

internal class WorkbenchWindowState (
    val title: String,
    val content: @Composable () -> Unit,
) {
}
