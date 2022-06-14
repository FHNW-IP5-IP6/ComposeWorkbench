package view

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import view.component.*
import view.themes.DarkColors
import view.themes.LightColors

@Composable
internal fun WorkbenchUI(model: WorkbenchModel, closeRequest: ()->Unit) {
    DragAndDropWindow(
        model = model,
        onCloseRequest = closeRequest,
        currentWindow = model.mainWindow,
        moduleReceiver = {
            model.moduleToWindow(module = it)
        },
        windowScope = { workbenchMenuBar(model) }
    ) {
        WorkbenchBody(model)
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
                        DropTarget(controller = leftExplorerController) {
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
                    DropTarget(controller = bottomExplorerController) {
                        WorkbenchTabRow(bottomExplorerController)
                    }
                }
            }
        }
    }
}
