package view

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Window
import controller.ExplorerController
import controller.WorkbenchController
import model.WorkbenchModel
import model.state.DisplayType
import model.state.WorkbenchModuleState

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
                ExplorerUi(model, ExplorerController(model)) {
                    TabSpace(model)
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
private fun TabSpace(model: WorkbenchModel){
    with(model){
        if (numberOfTabs() > 0) {
            Column {
                TabRow(selectedTabIndex = selectedTabIndex()) {
                    for (tab in modules) {
                        key(tab) {
                            if (tab.displayType == DisplayType.TAB)
                                Tab(
                                    selected = tab == selectedTab,
                                    onClick = { selectedTab = tab }
                                ) {
                                    TabWriter(tab, WorkbenchController(model))
                                }
                        }
                    }
                }
                selectedTab?.content()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TabWriter (tab: WorkbenchModuleState<*>, controller: WorkbenchController) {
    ContextMenuArea(items = {
        listOf(
            ContextMenuItem("Open in Window") { controller.convertToWindow(tab) },
        )
    }) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically)
        {
            Text(
                text = tab.title,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = tab::onClose) {
                Icon(Icons.Filled.Close, "close")
            }
        }
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