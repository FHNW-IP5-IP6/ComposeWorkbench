package view

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.Window
import model.WorkbenchModel


@Composable
internal fun WorkbenchMainUI(model: WorkbenchModel) {
    showWindows(model)
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
private fun TabSpace(model: WorkbenchModel){
    with(model){
        Column {
            TabRow(selectedTabIndex = getIndex(selectedExplorer)) {
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
internal fun showWindows(model: WorkbenchModel){
    with(model){
        windows.forEach {
            Window(
                onCloseRequest = { windows.remove(it) },
                title = it.title,
            ) {
                it.content.invoke()
            }
        }
    }
}