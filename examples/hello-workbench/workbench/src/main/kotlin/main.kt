import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
fun main() {

    val colors = listOf(Color.Green, Color.Red, Color.Gray, Color.Blue, Color.Cyan, Color.Magenta)

    val workbench = Workbench("Hello Workbench", true)

    workbench.registerExplorer<List<Color>>("Colors", { "Colors: ${it.size}" }, { _, _ -> }) {
        Column {
            it.forEachIndexed { i, c ->
                Card(
                    modifier = Modifier.padding(5.dp).height(25.dp).fillMaxWidth(),
                    backgroundColor = c,
                    onClick = { workbench.requestEditor<Color>("Color", i) }
                ) { }
            }
        }
    }

    workbench.requestExplorer("Colors", colors, listed = true)

    workbench.registerEditor<Color>("Color", { "${it.value}"}, {i, _ -> colors[i] }, Icons.Default.Settings){
        Card(
            modifier = Modifier.padding(5.dp).fillMaxSize(),
            backgroundColor = it
        )  { }
    }

    workbench.run {
        println("Exit Hello Workbench")
        success()
    }

}

