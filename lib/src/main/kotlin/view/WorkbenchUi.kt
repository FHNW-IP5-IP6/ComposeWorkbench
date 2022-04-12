package view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.WorkbenchModuleState
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
                topBar = { Bar() },
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
private fun Bar() {
    TopAppBar(title = { Text("Workbench Top Bar") })
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