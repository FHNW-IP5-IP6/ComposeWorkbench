package model.state

import MAIN_WINDOW_POS_OFFSET
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState

internal class WorkbenchWindowState(
    position: WindowPosition = WindowPosition(MAIN_WINDOW_POS_OFFSET, MAIN_WINDOW_POS_OFFSET)
) {
    val windowState by mutableStateOf(WindowState(position = position))
    var hasFocus by mutableStateOf(false)
    var windowHeaderOffset by mutableStateOf(0.dp)
}
