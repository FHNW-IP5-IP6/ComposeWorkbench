package view

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.WorkbenchModuleState
import util.selectedButtonColors


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
                ExplorerUi(model, explorerTabController) {
                    EditorTabSpace(model, editorTabController)
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
private fun EditorTabSpace(model: WorkbenchModel, controller: WorkbenchModuleController){
    if (controller.getModulesFiltered().isNotEmpty()) {
        Column {
            WorkbenchTabRow(controller)
            controller.getSelectedModule().value?.content()
        }
    }
}

@Composable
internal fun WorkbenchTabRow(controller: WorkbenchModuleController) {
    var tabScrollState = rememberScrollState()

    var modifier = Modifier.horizontalScroll(tabScrollState)

    if (controller.displayType.orientation.toInt() != 0) {
        modifier = Modifier.verticalScroll(tabScrollState)
            .layout() { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(placeable.height, placeable.width) {         //switch with and height
                    placeable.place(                                //place correctly
                        x = -(placeable.width / 2 - placeable.height / 2),
                        y = -(placeable.height / 2 - placeable.width / 2)
                    )
                }
            }.rotate(degrees = controller.displayType.orientation)
    }

    Row(modifier = modifier) {
        for (tab in controller.getModulesFiltered()) {
            TabWriter(
                tab = tab,
                controller,
                selected = controller.isModuleSelected(tab),
                onClick = { controller.moduleSelectorPressed(tab) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
private fun TabWriter (tab: WorkbenchModuleState<*>, controller: WorkbenchModuleController, selected: Boolean, onClick: ()->Unit) {

    val colors = ButtonDefaults.selectedButtonColors(selected)
    val backgroundColor = colors.backgroundColor(selected).value
    val contentColor = colors.contentColor(selected).value

    val writerModifier = Modifier
        .clickable {onClick()}
        .background(color = backgroundColor)
        .drawBehind {
            if (selected) {
                val height = size.height
                val width = size.width
                val strokeWidth = 10f
                val ypos = height - (strokeWidth/2)
                drawLine(
                    start = Offset(x = 0f, y = ypos),
                    end = Offset(x = width, y = ypos),
                    color = contentColor,
                    strokeWidth = strokeWidth
                )
            }
        }

    ContextMenuArea(items = {
        listOf(
            ContextMenuItem("Open in Window") { controller.convertToWindow(tab) },
        )
    }) {
        Row(
            modifier = writerModifier,
            verticalAlignment = Alignment.CenterVertically
        )
        {
            Text(
                text = tab.title,
                color = contentColor,
                overflow = TextOverflow.Visible,
                maxLines = 1,
                modifier = Modifier.padding(15.dp, 0.dp)
            )
            IconButton(onClick = tab::onClose) {
                Icon(Icons.Filled.Close, "close", tint = contentColor)
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