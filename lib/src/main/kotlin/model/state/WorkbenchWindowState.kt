package model.state

import MAIN_WINDOW_POS_OFFSET
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState

internal class WorkbenchWindowState(
    val windowState: WindowState,
    var windowHeaderOffset: Dp,
) {
}

internal fun getMainWorkbenchWindowState() :WorkbenchWindowState {
    return WorkbenchWindowState(
        windowState = WindowState(position = WindowPosition(MAIN_WINDOW_POS_OFFSET, MAIN_WINDOW_POS_OFFSET)),
        windowHeaderOffset = 0.dp
    )
}

