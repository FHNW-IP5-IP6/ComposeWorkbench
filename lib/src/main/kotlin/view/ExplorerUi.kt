package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import controller.ExplorerController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import util.selectedButtonColors
import util.vertical

@Composable
internal fun ExplorerUi(model: WorkbenchModel, controller: ExplorerController, editorView: @Composable () -> Unit){
    Row ( modifier = Modifier.fillMaxWidth()
    ) {
        ExplorerSelectors(model, controller)
        if(model.selectedExplorer != null){
            Box(modifier = Modifier.weight(2f)) { model.selectedExplorer!!.content() }
        }
        Box(modifier = Modifier.weight(3f).fillMaxWidth()){
            editorView()
        }
    }
}

@Composable
private fun ExplorerSelectors(model: WorkbenchModel, controller: ExplorerController) {
    Column(
        modifier = Modifier.padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (state in model.modules) {
            key(state) {
                if (state.displayType == DisplayType.RIGHT && state.module.moduleType == ModuleType.EXPLORER) {
                    val selected = controller.isExplorerSelected(state)
                    Button(
                        colors = ButtonDefaults.selectedButtonColors(selected),
                        modifier = Modifier.vertical(),
                        onClick = { controller.explorerSelectorPressed(state) }) {
                        Text(text = state.title)
                    }
                }
            }
        }
    }
}

