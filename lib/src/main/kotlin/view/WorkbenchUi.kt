package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.WorkbenchModuleState
import util.selectedButtonColors
import view.conponent.Resizable
import view.conponent.WorkbenchTabRow


@Composable
internal fun WorkbenchMainUI(model: WorkbenchModel, closeRequest: ()->Unit) {
    Window(
        onCloseRequest = closeRequest,
        title = model.appTitle
    ) {
        MaterialTheme {
            Scaffold(
                topBar = { Bar(model) },
            ) {
                val explorerTabController = WorkbenchModuleController(model, DisplayType.LEFT, ModuleType.EXPLORER, true)
                val editorTabController = WorkbenchModuleController(model, DisplayType.TAB, ModuleType.EDITOR)

                Row ( modifier = Modifier.fillMaxWidth()
                ) {
                    Resizable {
                        TabSpace(explorerTabController)
                    }
                    Box(modifier = Modifier.weight(3f).fillMaxWidth()){
                        TabSpace(editorTabController)
                    }
                }
            }
        }
    }
}

@Composable
private fun Bar(model: WorkbenchModel) {
    TopAppBar(content = {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            ) {
            Text("Workbench Top Bar",
                fontSize = 30.sp,
                color = Color.White,
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
            )
            Button(
                onClick = { model.saveAll(ModuleType.EDITOR) },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                colors = ButtonDefaults.selectedButtonColors(true)
            ) { Text("Save All") }
        }
    })
}

@Composable
private fun TabSpace(controller: WorkbenchModuleController){
    if (controller.getModulesFiltered().isNotEmpty()) {
        WorkbenchTabRow(controller)
    }
}

@Composable
internal fun WindowSpace(model: WorkbenchModel){
    for (window in model.modules) {
        key(window) {
            if (window.displayType == DisplayType.WINDOW) {
                WorkbenchWindow(window)
            }
        }
    }
}

@Composable
private fun WorkbenchWindow (state : WorkbenchModuleState<*>) {
    Window(
        onCloseRequest = state::onClose,
        title = state.title,
    ) {
        state.content()
    }
}