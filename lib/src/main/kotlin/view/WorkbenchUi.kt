package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController
import model.data.DisplayType
import model.data.ModuleType
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import view.component.*
import view.themes.DarkColors
import view.themes.LightColors

@Composable
internal fun WorkbenchUI(controller: WorkbenchController,
                         closeRequest: ()->Unit) {
    DragAndDropWindow(
        controller = controller,
        onCloseRequest = closeRequest,
        currentWindow = controller.getMainWindow(),
        moduleReceiver = {
            controller.moduleToWindow(moduleState = it)
        },
        windowScope = { workbenchMenuBar(controller.commandController) }
    ) {
        WorkbenchBody(controller)
    }
}

@Composable
@OptIn(ExperimentalSplitPaneApi::class)
private fun WorkbenchBody(controller: WorkbenchController) {
    MaterialTheme(
        colors = if (false) DarkColors else LightColors,
        typography = NotoSansTypography,
    ) {
        Scaffold(
            topBar = { WorkbenchAppBar(controller) },
        ) {
            val leftExplorerController = controller.createModuleDisplayController(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, deselectable = true)
            val bottomExplorerController = controller.createModuleDisplayController(displayType = DisplayType.BOTTOM, moduleType = ModuleType.EXPLORER, deselectable = true)
            Column {
                BoxWithConstraints {
                    Row(modifier = Modifier.height(maxHeight - bottomExplorerController.getTabRowMinDimension().second)) {
                        DropTarget(controller = leftExplorerController) {
                            WorkbenchTabRow(leftExplorerController, controller)
                        }
                        WorkbenchVerticalSplitPane(splitPaneState = controller.getBottomSplitState()) {
                            first {
                                WorkbenchHorizontalSplitPane(splitPaneState = controller.getLeftSplitState()){
                                    first {
                                        WorkbenchTabBody(leftExplorerController, controller)
                                    }
                                    second {
                                        WorkbenchEditorSpace(controller)
                                    }
                                }
                            }
                            second {
                                WorkbenchTabBody(bottomExplorerController, controller)
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .height(bottomExplorerController.getTabRowMinDimension().second).padding(start = bottomExplorerController.getTabRowMinDimension().first)
                        .align(Alignment.End)
                ) {
                    DropTarget(controller = bottomExplorerController) {
                        WorkbenchTabRow(bottomExplorerController, controller)
                    }
                }
            }
        }
    }
}
