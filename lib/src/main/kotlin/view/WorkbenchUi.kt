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
<<<<<<< HEAD
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
=======
import androidx.compose.ui.graphics.SolidColor
>>>>>>> 710910cd1240b46ec43de73031e8ae9ec130f561
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import util.cursorForHorizontalResize
import util.cursorForVerticalResize
import util.selectedButtonColors
import view.conponent.WorkbenchTabRow

<<<<<<< HEAD
@OptIn(ExperimentalComposeUiApi::class)
=======


>>>>>>> 710910cd1240b46ec43de73031e8ae9ec130f561
@Composable
internal fun WorkbenchMainUI(model: WorkbenchModel, closeRequest: ()->Unit) {
    Window(
        onCloseRequest = closeRequest,
        title = model.appTitle
    ) {
        MenuBar {
            Menu("View", mnemonic = 'V') {
                Menu("Split") {
                    Item("Horizontal", onClick = { println("Horizontal Split for TabSpace") }, shortcut = KeyShortcut(Key.H, ctrl = true))
                    Item("Vertical", onClick = { println("Vertical Split for TabSpace") }, shortcut = KeyShortcut(Key.V, ctrl = true))
                }
            }
        }
        MaterialTheme {
            Scaffold(
                topBar = { Bar(model) },
            ) {
                val leftExplorerController = WorkbenchModuleController(model, DisplayType.LEFT, ModuleType.EXPLORER, true)
                val bottomExplorerController = WorkbenchModuleController(model, DisplayType.BOTTOM, ModuleType.EXPLORER, true)
                val editorTabController = WorkbenchModuleController(model, DisplayType.TAB, ModuleType.EDITOR)

                VerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.7f)) {
                    first {
                        HorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = 0.25f)) {
                            first() {
                                TabSpace(leftExplorerController)
                            }
                            second() {
                                TabSpace(editorTabController)
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
                    second {
                        TabSpace(bottomExplorerController)
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