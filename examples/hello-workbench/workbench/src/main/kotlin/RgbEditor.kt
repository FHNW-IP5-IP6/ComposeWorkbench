
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun RgbEditorUi(controller: RgbController){
    Card(
        modifier = Modifier.padding(5.dp).fillMaxSize(),
        backgroundColor = Color(controller.rgbState.r, controller.rgbState.g, controller.rgbState.b, 1f)
    )  {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ColorTextField(label = "R", value = controller.rgbState.r.toString()){controller.triggerAction(RgbAction.UpdateG(it))}
            ColorTextField(label = "G", value = controller.rgbState.g.toString()){controller.triggerAction(RgbAction.UpdateG(it))}
            ColorTextField(label = "B", value = controller.rgbState.b.toString()){controller.triggerAction(RgbAction.UpdateG(it))}
        }
    }
}

@Composable
private fun ColorTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    TextField(
        modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
        shape = RoundedCornerShape(15.dp),
        label = { Text(text = label, color = Color.DarkGray) },
        value = value,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            focusedIndicatorColor =  Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        onValueChange = onValueChange
    )
}

internal class RgbController(index: Int, color: Color, private val publishChange: () -> Unit) {
    var rgbState by mutableStateOf(RgbState(index, color.red, color.green, color.blue))

    fun triggerAction(action: RgbAction) {
        rgbState = when(action) {
            is RgbAction.UpdateR -> rgbState.copy(r = action.r.toRgbOrDefault(0f))
            is RgbAction.UpdateG -> rgbState.copy(g = action.g.toRgbOrDefault(0f))
            is RgbAction.UpdateB -> rgbState.copy(b = action.b.toRgbOrDefault(0f))
        }
        publishChange.invoke()
    }
}

internal data class RgbState(
    val index: Int,
    val r: Float,
    val g: Float,
    val b: Float
){
    fun title() = "R: $r, G: $g, B: $b"
}

internal sealed class RgbAction() {
    class UpdateR(val r: String) :RgbAction()
    class UpdateG(val g: String) :RgbAction()
    class UpdateB(val b: String) :RgbAction()
}

internal fun String.toRgbOrDefault(default: Float) = if (this.toFloatOrNull() == null) default else this.toFloatOrNull()!!.coerceIn(0f,1f)