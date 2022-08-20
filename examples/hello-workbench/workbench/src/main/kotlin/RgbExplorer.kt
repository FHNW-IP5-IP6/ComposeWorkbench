import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun RgbExplorerUI( colors : List<Color>, onColorSelect: (id: Int)->Unit) {
    Column {
        colors.forEachIndexed { i, c ->
            Card (
                modifier = Modifier.padding(5.dp).height(25.dp).fillMaxWidth(),
                backgroundColor = c,
                onClick = { onColorSelect(i) }
            ) { }
        }
    }
}