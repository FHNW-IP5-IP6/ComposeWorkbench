package util

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.window.WindowPosition
import java.awt.Cursor

//https://stackoverflow.com/questions/70057396/how-to-show-vertical-text-with-proper-size-layout-in-jetpack-compose
internal fun Modifier.vertical(degrees: Float) =
    this.then(
        layout() { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.height, placeable.width) {         //switch with and height
                placeable.place(                                //place correctly
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        }
    ).then(this.rotate(degrees))

@Composable
internal fun ButtonDefaults.selectedButtonColors(selected: Boolean) =
    if (selected) {
        this.outlinedButtonColors(backgroundColor = Color.LightGray) //use color definition file
    } else {
        this.outlinedButtonColors()
    }

internal fun DpOffset.toOffset() = Offset(this.x.value, this.y.value)

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.cursorForVerticalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.cursorForClickable(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))

internal fun WindowPosition.toDpOffset(): DpOffset = DpOffset(x, y)
