package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import util.selectedButtonColors
import util.vertical

@Composable
internal fun ExplorerUi(model: WorkbenchModel, controller: WorkbenchModuleController, editorView: @Composable () -> Unit){
    Row ( modifier = Modifier.fillMaxWidth()
    ) {
        WorkbenchTabRow(controller)
        if(controller.getSelectedModule().value != null){
            Box(modifier = Modifier.weight(2f)) { controller.getSelectedModule().value!!.content()}
        }
        Box(modifier = Modifier.weight(3f).fillMaxWidth()){
            editorView()
        }
    }
}

