package util

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import java.awt.event.MouseEvent
import kotlin.math.absoluteValue

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

internal fun MouseEvent.intOffset(): IntOffset = IntOffset(this.x, this.y)

@Composable
internal fun Modifier.onDragEvent(
    pass: PointerEventPass = PointerEventPass.Main,
    onRelease: AwaitPointerEventScope.(event: PointerEvent) -> Unit,
    onEvent: AwaitPointerEventScope.(yMovement: Dp, xMovement: Dp) -> Unit,
) = pointerInput(PointerEventType.Move, PointerEventPass.Main, onEvent) {
    awaitPointerEventScope {
        val doIfPrimary: (e: PointerEvent, f: () -> Unit) -> Unit = {e, f -> if (e.buttons.isPrimaryPressed) f.invoke()}
        data class Props(val isDragged: Boolean = false, val position: IntOffset =  IntOffset(0,0))
        var info = Props()

        while (true) {
            val event = awaitPointerEvent(pass)
            when(event.type) {
                PointerEventType.Press ->  doIfPrimary(event) {info = info.copy(position = event.awtEvent.intOffset())}
                PointerEventType.Move ->  doIfPrimary(event) {
                    val dragDist = event.awtEvent.intOffset().minus(info.position)
                    if(dragDist.x.absoluteValue > 5){
                        info = Props(isDragged = true, position = event.awtEvent.intOffset())
                        onEvent(dragDist.x.dp, dragDist.y.dp)
                    }
                }
                PointerEventType.Release -> {
                    if(info.isDragged) onRelease(event)
                    info = Props()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.cursorForHorizontalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.E_RESIZE_CURSOR)))

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.cursorForVerticalResize(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.N_RESIZE_CURSOR)))

@OptIn(ExperimentalComposeUiApi::class)
internal fun Modifier.cursorForClickable(): Modifier =
    pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
