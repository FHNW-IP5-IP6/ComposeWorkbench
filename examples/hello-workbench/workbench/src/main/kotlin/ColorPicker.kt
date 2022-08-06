
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIconDefaults
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ColorPickerUi(controller: ColorPickerController){
    Column {
        Card(
            modifier = Modifier.height(50.dp).fillMaxWidth()
                .padding(5.dp),
            backgroundColor = controller.state.actualColor,
            elevation = 10.dp,
        ) {
            Text(modifier = Modifier.fillMaxSize() ,text = "Selected Color", color = Color.White, textAlign = TextAlign.Center )
        }
        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.Center)
                .fillMaxSize()
                .padding(5.dp)
                .background(color = controller.state.mouseColor, shape = RoundedCornerShape(15.dp))
                .pointerHoverIcon(icon = PointerIconDefaults.Crosshair)
                .onPointerEvent(PointerEventType.Move) {
                    val position = it.changes.first().position
                    controller.triggerAction(
                        ColorPickerAction.UpdateMouseColor(
                            Color(
                                position.x.toInt() % 256,
                                position.y.toInt() % 256,
                                0
                            )
                        )
                    )
                }.onPointerEvent(PointerEventType.Press) {
                    controller.triggerAction(ColorPickerAction.PickColor())
                }
        )
    }
}

internal class ColorPickerController(index: Int, color: Color, private val publishChange: () -> Unit) {
    var state by mutableStateOf(ColorPickerState(index, color, color))

     fun triggerAction(action: ColorPickerAction) {
        state = when(action) {
            is ColorPickerAction.UpdateMouseColor -> state.copy(mouseColor = action.c)
            is ColorPickerAction.PickColor -> state.copy(actualColor = state.mouseColor)
        }
         publishChange.invoke()
    }
}

internal data class ColorPickerState(
    val index: Int,
    val actualColor: Color,
    val mouseColor: Color,
){
    fun title() = actualColor.colorSpace.name
}

internal sealed class ColorPickerAction() {
    class UpdateMouseColor(val c: Color)    :ColorPickerAction()
    class PickColor()                       :ColorPickerAction()
}
