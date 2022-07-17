package view

import TAB_ROW_WIDTH
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController
import model.data.TabRowKey
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
        tabRowKey = TabRowKey(displayType = DisplayType.WINDOW, ModuleType.BOTH, controller.getMainWindow()),
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
            val leftExplorerTabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = controller.getMainWindow())
            val bottomExplorerTabRowKey = TabRowKey(displayType = DisplayType.BOTTOM, moduleType = ModuleType.EXPLORER, windowState = controller.getMainWindow())
            val hasBottomTabs = controller.hasModules(bottomExplorerTabRowKey)
            Column {
                Row(modifier = Modifier.weight(if(hasBottomTabs) 0.9f else 1f)
                ) {
                    DropTarget(tabRowKey = leftExplorerTabRowKey, controller = controller) {
                        WorkbenchTabRow(leftExplorerTabRowKey, controller)
                    }
                    WorkbenchVerticalSplitPane(splitPaneState = controller.informationState.bottomSplitState) {
                        first {
                            WorkbenchHorizontalSplitPane(splitPaneState = controller.informationState.leftSplitState){
                                first {
                                    WorkbenchTabBody(leftExplorerTabRowKey, controller)
                                }
                                second {
                                    WorkbenchEditorSpace(controller)
                                }
                            }
                        }
                        second {
                            WorkbenchTabBody(bottomExplorerTabRowKey, controller)
                        }
                    }
                }
                Row(
                    modifier = Modifier.weight(if(hasBottomTabs) 0.1f else 0.001f)
                        .padding(start = TAB_ROW_WIDTH.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Start
                ) {
                    DropTarget(tabRowKey = bottomExplorerTabRowKey, controller = controller) {
                        WorkbenchTabRow(bottomExplorerTabRowKey, controller)
                    }
                }
            }
        }
    }
}