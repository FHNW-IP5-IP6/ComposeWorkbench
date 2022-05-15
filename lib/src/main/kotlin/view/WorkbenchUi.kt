package view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.SplitViewMode
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import util.cursorForHorizontalResize
import util.cursorForVerticalResize
import util.selectedButtonColors
import view.conponent.WorkbenchTabBody
import view.conponent.WorkbenchTabRow

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchMainUI(model: WorkbenchModel, closeRequest: ()->Unit) {
    Window(
        onCloseRequest = closeRequest,
        title = model.appTitle
    ) {
        MenuBar {
            Menu("File", mnemonic = 'F') {
                Item("Save All", onClick = { model.saveAll(ModuleType.EDITOR) }, shortcut = KeyShortcut(Key.S, ctrl = true, alt = true))
                Item("Show Default Explorers", onClick = {model.showDefaultExplorersOverview()})
            }
            Menu("View", mnemonic = 'V') {
                Menu("Split TabSpace") {
                    Item("Horizontal", onClick = { model.changeSplitViewMode(SplitViewMode.HORIZONTAL) }, shortcut = KeyShortcut(Key.H, ctrl = true))
                    Item("Vertical", onClick = { model.changeSplitViewMode(SplitViewMode.VERTICAL) }, shortcut = KeyShortcut(Key.V, ctrl = true))
                    Item("Unsplit", onClick = { model.changeSplitViewMode(SplitViewMode.UNSPLIT) }, shortcut = KeyShortcut(Key.U, ctrl = true))
                }
            }
        }
        MaterialTheme {
            Scaffold(
                topBar = { Bar(model) },
            ) {
                val leftExplorerController = WorkbenchModuleController(model, DisplayType.LEFT, ModuleType.EXPLORER, true)
                val bottomExplorerController = WorkbenchModuleController(model, DisplayType.BOTTOM, ModuleType.EXPLORER, true)


                Column {
                    Row(modifier = Modifier.weight(0.9f)) {
                        TabRow(leftExplorerController)
                        VerticalSplitPane(splitPaneState = model.bottomSplitState) {
                            first {
                                HorizontalSplitPane(splitPaneState = model.leftSplitState) {
                                    first() {
                                        TabBody(leftExplorerController)
                                    }
                                    second() {
                                        TabSpaceEditor(model)
                                    }
                                    splitter {
                                        visiblePart {
                                            Box(
                                                modifier = Modifier.width(2.dp).fillMaxHeight()
                                                    .background(SolidColor(Color.Gray), alpha = 0.50f)
                                            )
                                        }
                                        handle {
                                            Box(
                                                modifier = Modifier.markAsHandle().cursorForHorizontalResize()
                                                    .width(9.dp)
                                                    .fillMaxHeight()
                                            )
                                        }
                                    }
                                }
                            }
                            second() {
                                TabBody(bottomExplorerController)
                            }
                            splitter {
                                visiblePart {
                                    Box(
                                        modifier = Modifier.height(2.dp).fillMaxWidth()
                                            .background(SolidColor(Color.Gray), alpha = 0.50f)
                                    )
                                }
                                handle {
                                    Box(
                                        modifier = Modifier.markAsHandle().cursorForVerticalResize().height(9.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    Box (modifier = Modifier.weight(0.1f, fill = false).fillMaxWidth()){
                        TabRow(bottomExplorerController)
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
private fun TabRow(controller: WorkbenchModuleController){
    if (controller.getModulesFiltered().isNotEmpty()) {
        WorkbenchTabRow(controller)
    }else{
        Box{} //empty box for split pane to work
    }
}

@Composable
private fun TabSpace(controller: WorkbenchModuleController){
    if (controller.getModulesFiltered().isNotEmpty()) {
        Column {
            WorkbenchTabRow(controller)
            WorkbenchTabBody(controller)
        }
    }else{
        Box{} //empty box for split pane to work
    }
}


@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun TabSpaceEditor(model: WorkbenchModel){
    val editorTabController1 = WorkbenchModuleController(model, DisplayType.TAB1, ModuleType.EDITOR)
    val editorTabController2 = WorkbenchModuleController(model, DisplayType.TAB2, ModuleType.EDITOR)
    var splitRatio: Float = .5f
    if (editorTabController1.getModulesFiltered().isEmpty()) splitRatio = 0f
    if (editorTabController2.getModulesFiltered().isEmpty()) splitRatio = 1f

    if (model.splitViewMode == SplitViewMode.VERTICAL) {
        VerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
            first {
                TabSpace(editorTabController1)
            }
            second {
                TabSpace(editorTabController2)
            }
            splitter {
                visiblePart {
                    Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(SolidColor(Color.Gray), alpha = 0.50f))
                }
                handle {
                    Box(modifier = Modifier.markAsHandle().cursorForVerticalResize().height(9.dp).fillMaxWidth())
                }
            }
        }
    } else if (model.splitViewMode == SplitViewMode.HORIZONTAL) {
        HorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
            first {
                TabSpace(editorTabController1)
            }
            second {
                TabSpace(editorTabController2)
            }
            splitter {
                visiblePart {
                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(SolidColor(Color.Gray), alpha = 0.50f))
                }
                handle {
                    Box(modifier = Modifier.markAsHandle().cursorForHorizontalResize().width(9.dp).fillMaxHeight())
                }
            }
        }
    }
    else
    {
        TabSpace(editorTabController1)
    }
}

@Composable
private fun TabBody(controller: WorkbenchModuleController){
    println(controller.getSelectedModule())
    if (controller.getSelectedModule().value != null) {
        WorkbenchTabBody(controller)
    }else{
        Box{} //empty box for split pane to work
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