package view.conponent

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import util.onDragEvent
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun Resizable(
    state: ResizeState = rememberResizeState(),
    content: @Composable () -> Unit
){
    Row {
        Box(modifier = Modifier.width(state.value)) {
            content()
        }
        Divider(
            color = Color.Gray,
            modifier = Modifier.width(2.dp).fillMaxHeight()
                .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR)))
                .onDragEvent(onRelease = {}) { x, _ ->
                    state.resize(x)
                }
        )
    }
}

@Composable
internal fun rememberResizeState(initial: Dp = 300.dp): ResizeState {
    return rememberSaveable(saver = ResizeState.Saver) {
        ResizeState(initial = initial)
    }
}
internal class ResizeState(initial: Dp) {
    /**
     * current content size in DP
     */
    var value: Dp by mutableStateOf(initial, structuralEqualityPolicy())
    private set

    fun resize(change: Dp){
        value += change
    }

    companion object {
        /**
         * The default [Saver] implementation for [ResizeState].
         */
        val Saver: Saver<ResizeState, *> = Saver(
            save = { it.value },
            restore = { ResizeState(it) }
        )
    }
}