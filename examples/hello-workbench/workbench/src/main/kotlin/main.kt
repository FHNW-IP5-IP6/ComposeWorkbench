
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

const val COLORS = "colors"
const val COLOR = "color"

@OptIn(ExperimentalMaterialApi::class)
fun main() {

    val colorRepository = mutableListOf<Color>(Color(0xff182260), Color(0xff9C27B0), Color(0xff009688), Color(0xff7986CB), Color(0xff0069e0) , Color(0xff8fd3d8), Color(0xffF76c6c), Color(0xff333333))
    val workbench = Workbench("Hello Workbench", true)

    workbench.registerExplorer<List<Color>>(
        type = COLORS,
        title = { colors -> "Colors: ${colors.size}" },
        init = { _, _ -> }
    ) { colors ->
        Column {
            colors.forEachIndexed { i, c ->
                Card (
                    modifier = Modifier.padding(5.dp).height(25.dp).fillMaxWidth(),
                    backgroundColor = c,
                    onClick = { workbench.requestEditor(
                        type = COLOR,
                        id = i
                    ) }
                ) { }
            }
        }
    }

    workbench.requestExplorer<List<Color>>(type = COLORS, c = colorRepository, listed = true)

    workbench.registerEditor<RgbController>(
        type =COLOR,
        title = { it.rgbState.title() },
        initController =  {i, mqtt -> RgbController(i, colorRepository[i]){mqtt.publishUnsaved(COLOR, i)} },
        icon = Icons.Filled.Edit,
        onSave = { c, _ ->
            colorRepository[c.rgbState.index] = Color(c.rgbState.r, c.rgbState.g, c.rgbState.b, 1f)
            success()
        }
    ){
        RgbEditorUi(it)
    }

    workbench.registerEditor<ColorPickerController>(
        type = COLOR,
        title = { it.state.title() },
        initController = {i, mqtt -> ColorPickerController(i, colorRepository[i]){mqtt.publishUnsaved(COLOR, i)} },
        icon = Icons.Filled.Palette,
        onSave = { c, _ ->
            colorRepository[c.state.index] = c.state.actualColor
            success()
        }
    ){
        ColorPickerUi(it)
    }

    workbench.run {
        println("Exit Hello Workbench")
        success()
    }

}


