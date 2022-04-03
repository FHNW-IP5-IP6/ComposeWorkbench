package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import controller.ExplorerController
import model.WorkbenchModel
import util.selectedElevation
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
private fun ExplorerSelectors(model: WorkbenchModel, controller: ExplorerController){
    Column (
        modifier = Modifier.padding(top = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        model.explorers.forEach {
            Button(
                elevation = if (controller.isExplorerSelected(it)) ButtonDefaults.selectedElevation() else ButtonDefaults.elevation() ,
                colors = ButtonDefaults.outlinedButtonColors(),
                modifier = Modifier.vertical().rotate(-90f),
                onClick = {controller.explorerSelectorPressed(it)}){
                Text(text = it.title)
            }
        }
    }
}

