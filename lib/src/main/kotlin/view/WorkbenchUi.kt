package view

import SPLIT_PAIN_HANDLE_ALPHA
import SPLIT_PAIN_HANDLE_AREA
import SPLIT_PAIN_HANDLE_SIZE
import SPLIT_PAIN_MIN_SIZE
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
import util.cursorForHorizontalResize
import util.cursorForVerticalResize
import util.selectedButtonColors
import view.conponent.*

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun WorkbenchMainUI(model: WorkbenchModel, closeRequest: ()->Unit) {
    Window(
        onCloseRequest = closeRequest,
        title = model.appTitle
    ) {
        MenuBar {
            Menu("File", mnemonic = 'F') {
                Item(
                    "Save All",
                    onClick = { model.saveAll(ModuleType.EDITOR) },
                    shortcut = KeyShortcut(Key.S, ctrl = true, alt = true)
                )
                Item("Show Default Explorers", onClick = { model.showDefaultExplorersOverview() })
            }
            Menu("View", mnemonic = 'V') {
                Menu("Split TabSpace") {
                    Item(
                        "Horizontal",
                        onClick = { model.changeSplitViewMode(SplitViewMode.HORIZONTAL) },
                        shortcut = KeyShortcut(Key.H, ctrl = true)
                    )
                    Item(
                        "Vertical",
                        onClick = { model.changeSplitViewMode(SplitViewMode.VERTICAL) },
                        shortcut = KeyShortcut(Key.V, ctrl = true)
                    )
                    Item(
                        "Unsplit",
                        onClick = { model.changeSplitViewMode(SplitViewMode.UNSPLIT) },
                        shortcut = KeyShortcut(Key.U, ctrl = true)
                    )
                }
            }
        }
        DragAndDropContainer(model) {
            WorkbenchBody(model)
        }
    }
}

@Composable
@OptIn(ExperimentalSplitPaneApi::class)
private fun WorkbenchBody(model: WorkbenchModel) {
    MaterialTheme {
        Scaffold(
            topBar = { Bar(model) },
        ) {
            val leftExplorerController =
                WorkbenchModuleController(model, DisplayType.LEFT, ModuleType.EXPLORER, true)
            val bottomExplorerController =
                WorkbenchModuleController(model, DisplayType.BOTTOM, ModuleType.EXPLORER, true)
            Column {
                BoxWithConstraints {
                    Row(modifier = Modifier.height(maxHeight - bottomExplorerController.getTabRowMinDimension().second)) {
                        DropTarget(dropTargetType = DisplayType.LEFT, acceptedType = ModuleType.EXPLORER, model = model, moduleReceiver = { leftExplorerController.updateDisplayType(it, DisplayType.LEFT) }) {
                            WorkbenchTabRow(leftExplorerController)
                        }
                        VerticalSplitPane(splitPaneState = model.bottomSplitState) {
                            first {
                                HorizontalSplitPane(model, leftExplorerController)
                            }
                            second(minSize = SPLIT_PAIN_MIN_SIZE) {
                                WorkbenchTabBody(bottomExplorerController)
                            }
                            splitter {
                                visiblePart {
                                    Box(
                                        modifier = Modifier.height(SPLIT_PAIN_HANDLE_SIZE).fillMaxWidth()
                                            .background(SolidColor(Color.Gray), alpha = SPLIT_PAIN_HANDLE_ALPHA)
                                    )
                                }
                                handle {
                                    Box(
                                        modifier = Modifier.markAsHandle().cursorForVerticalResize()
                                            .height(SPLIT_PAIN_HANDLE_AREA)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(bottomExplorerController.getTabRowMinDimension().second).padding(start = bottomExplorerController.getTabRowMinDimension().first)
                        .align(Alignment.End)
                ) {
                    DropTarget(dropTargetType = DisplayType.BOTTOM, acceptedType = ModuleType.EXPLORER,model = model, moduleReceiver = { bottomExplorerController.updateDisplayType(it, DisplayType.BOTTOM) }) {
                        WorkbenchTabRow(bottomExplorerController)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSplitPaneApi::class)
@Composable
private fun HorizontalSplitPane(
    model: WorkbenchModel,
    leftExplorerController: WorkbenchModuleController
) {
    HorizontalSplitPane(splitPaneState = model.leftSplitState) {
        first() {
            WorkbenchTabBody(leftExplorerController)
        }
        second() {
            EditorTabSpace(model)
        }
        splitter {
            visiblePart {
                Box(
                    modifier = Modifier.width(SPLIT_PAIN_HANDLE_SIZE).fillMaxHeight()
                        .background(SolidColor(Color.Gray), alpha = SPLIT_PAIN_HANDLE_ALPHA)
                )
            }
            handle {
                Box(
                    modifier = Modifier.markAsHandle().cursorForHorizontalResize()
                        .width(SPLIT_PAIN_HANDLE_AREA)
                        .fillMaxHeight()
                )
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
        title = state.getTitle(),
        state = state.getWindowState()
    ) {
        state.content()
    }
}