package view

import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import controller.ExplorerController
import controller.WindowsController
import model.WorkbenchModel

//TODO: Split Ui into editor and explorer ui
@Composable
internal fun WorkbenchMainUI(model: WorkbenchModel) {
    WindowSpace(model)
    MaterialTheme {
        Scaffold(
            topBar = { Bar() },
        ){
            TabSpace(model)
        }
    }
}

@Composable
private fun Bar() {
    TopAppBar(title = { Text("Workbench Top Bar") })
}

@Composable
private fun TabSpace(model: WorkbenchModel){
    with(model){
        Column {
            TabRow(selectedTabIndex = ExplorerController(model).getIndex(selectedExplorer)) {
                explorers.forEach { explorer ->
                    Tab(
                        text = { Text(explorer.title) },
                        selected = explorer == selectedExplorer,
                        onClick = { selectedExplorer = explorer }
                    )
                }
            }
            selectedExplorer?.content?.invoke()
        }
    }
}

@Composable
internal fun WindowSpace(model: WorkbenchModel){
    with(model){
        windows.forEach {
            Window(
                onCloseRequest = { WindowsController(model).remove(it) },
                title = it.title,
                state = it.windowState,
            ) {
                it.contentHolder.content()
            }
        }
    }
}