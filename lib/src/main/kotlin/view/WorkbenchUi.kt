package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController
import model.data.enums.DisplayType
import model.data.enums.ModuleType
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
            val leftExplorerController = controller.getDisplayController(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, deselectable = true)
            val bottomExplorerController = controller.getDisplayController(displayType = DisplayType.BOTTOM, moduleType = ModuleType.EXPLORER, deselectable = true)
            Column {
                BoxWithConstraints {
                    Row(modifier = Modifier.height(maxHeight - bottomExplorerController.getTabRowMinDimension().second)) {
                        DropTarget(displayController = leftExplorerController, controller = controller) {
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
                    DropTarget(displayController = bottomExplorerController, controller = controller) {
                        WorkbenchTabRow(bottomExplorerController, controller)
                    }
                }
            }
        }
    }
}
