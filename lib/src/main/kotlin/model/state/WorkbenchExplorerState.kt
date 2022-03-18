package model.state

import androidx.compose.runtime.Composable

internal class WorkbenchExplorerState(
    val title: String,
    val content: @Composable () -> Unit,
) {
}