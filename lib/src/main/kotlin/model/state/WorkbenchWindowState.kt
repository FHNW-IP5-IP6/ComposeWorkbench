package model.state

import androidx.compose.ui.window.WindowState
import model.ContentHolder

internal class WorkbenchWindowState (
    val title: String,
    val windowState: WindowState,
    val contentHolder: ContentHolder,
) {
}
