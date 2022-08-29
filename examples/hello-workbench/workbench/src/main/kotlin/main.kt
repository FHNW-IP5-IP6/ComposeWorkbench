
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.ui.graphics.Color

const val COLORS = "colors"
const val COLOR = "color"

fun main() {
    val colorRepository = mutableListOf(Color(0xff55efc4), Color(0xff74b9ff), Color(0xffa29bfe), Color(0xfffdcb6e), Color(0xffd63031) , Color(0xffdfe6e9), Color(0xff636e72), Color(0xff2d3436))
    val workbench = Workbench("Hello Workbench", true)

    workbench.registerExplorer<List<Color>>(
        type = COLORS,
        title = { colors -> "Colors: ${colors.size}" },
        init = { _, _ -> },
        explorerView = { colors -> RgbExplorerUI(colors) { workbench.requestEditor(type = COLOR, id = it) } }
    )
    workbench.requestExplorer<List<Color>>(type = COLORS, c = colorRepository, listed = true)

    workbench.registerEditor<RgbController>(
        type = COLOR,
        title = { it.rgbState.title() },
        initController =  {i, mqtt -> RgbController(i, colorRepository[i]){mqtt.publishUnsaved(COLOR, i)} },
        icon = Icons.Filled.Edit,
        onSave = { c, _ ->
            colorRepository[c.rgbState.index] = Color(c.rgbState.r, c.rgbState.g, c.rgbState.b, 1f)
            success()
        },
        editorView = { RgbEditorUi(it) }
    )

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

    workbench.run { println("Exit Workbench"); success() }
}


