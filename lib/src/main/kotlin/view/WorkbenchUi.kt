package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import view.conponent.*
import view.themes.DarkColors
import view.themes.LightColors

@Composable
internal fun WorkbenchMainUI(model: WorkbenchModel, closeRequest: ()->Unit) {
    Window(
        onCloseRequest = closeRequest,
        title = model.appTitle
    ) {
        workbenchMenuBar(model)
        DragAndDropContainer(model) {
            WorkbenchBody(model)
        }
    }
}

@Composable
@OptIn(ExperimentalSplitPaneApi::class)
private fun WorkbenchBody(model: WorkbenchModel) {

    MaterialTheme(
        colors = if (false) DarkColors else LightColors,
        typography = NotoSansTypography,
    ) {
        Scaffold(
            topBar = { WorkbenchAppBar(model) },
        ) {
            val leftExplorerController =
                WorkbenchModuleController(model, DisplayType.LEFT, ModuleType.EXPLORER, true)
            val bottomExplorerController =
                WorkbenchModuleController(model, DisplayType.BOTTOM, ModuleType.EXPLORER, true)
            Column {
                BoxWithConstraints {
                    Row(modifier = Modifier.height(maxHeight - bottomExplorerController.getTabRowMinDimension().second)) {
                        DropTarget(model = model, dropTargetType = DisplayType.LEFT, acceptedType = ModuleType.EXPLORER, moduleReceiver = { leftExplorerController.updateDisplayType(it, DisplayType.LEFT) }) {
                            WorkbenchTabRow(leftExplorerController)
                        }
                        WorkbenchVerticalSplitPane(splitPaneState = model.bottomSplitState) {
                            first {
                                WorkbenchHorizontalSplitPane(splitPaneState = model.leftSplitState){
                                    first {
                                        WorkbenchTabBody(leftExplorerController)
                                    }
                                    second {
                                        WorkbenchEditorSpace(model)
                                    }
                                }
                            }
                            second {
                                WorkbenchTabBody(bottomExplorerController)
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(bottomExplorerController.getTabRowMinDimension().second).padding(start = bottomExplorerController.getTabRowMinDimension().first)
                        .align(Alignment.End)
                ) {
                    DropTarget(model = model, dropTargetType = DisplayType.BOTTOM, acceptedType = ModuleType.EXPLORER, moduleReceiver = { bottomExplorerController.updateDisplayType(it, DisplayType.BOTTOM) }) {
                        WorkbenchTabRow(bottomExplorerController)
                    }
                }
            }
        }
    }
}

@Composable
internal fun WindowSpace(model: WorkbenchModel){
    key(model.windows) {
        for (state in model.windows) {
            Window(
                onCloseRequest = {
                    //TODO: bring back the module on close??
                    state.moduleState.onClose()
                    model.windows.remove(state) },
                title = state.moduleState.getTitle(),
                state = state.windowState
            ) {
                state.moduleState.content()
            }
        }
    }
}