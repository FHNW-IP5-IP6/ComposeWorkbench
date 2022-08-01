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
import controller.WorkbenchDragController
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.WorkbenchState
import model.state.WorkbenchInformationState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import view.component.*
import view.themes.LightColors

@Composable
internal fun WorkbenchUI(
    informationState: WorkbenchInformationState,
    workbenchState: WorkbenchState,
    closeRequest: ()->Unit
) {
    MaterialTheme(
        colors = LightColors,
        typography = NotoSansTypography,
    ) {
        DragAndDropWindow(
            informationState = informationState,
            dragState = WorkbenchDragController.dragState,
            onCloseRequest = closeRequest,
            tabRowKey = TabRowKey(
                displayType = DisplayType.WINDOW,
                ModuleType.BOTH,
                informationState.mainWindow
            ),
            windowScope = { workbenchMenuBar(informationState) }
        ) {
            when (workbenchState) {
                WorkbenchState.RUNNING -> {
                    WorkbenchBody(informationState)
                }
                WorkbenchState.STARTING -> {
                    WorkbenchStateInfo("Application is starting.")
                }
                WorkbenchState.TERMINATING -> {
                    WorkbenchStateInfo("Application is terminating.")
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalSplitPaneApi::class)
private fun WorkbenchBody(informationState: WorkbenchInformationState) {
    Scaffold(
        topBar = { WorkbenchAppBar(informationState) },
    ) {
        val leftExplorerTabRowKey = TabRowKey(
            displayType = DisplayType.LEFT,
            moduleType = ModuleType.EXPLORER,
            windowState = informationState.mainWindow
        )
        val bottomExplorerTabRowKey = TabRowKey(
            displayType = DisplayType.BOTTOM,
            moduleType = ModuleType.EXPLORER,
            windowState = informationState.mainWindow
        )
        val hasBottomTabs = informationState.hasModules(bottomExplorerTabRowKey)
        println("main Ui")
        Column {
            Row(
                modifier = Modifier.weight(if (hasBottomTabs) 0.9f else 1f)
            ) {
                DropTarget(tabRowKey = leftExplorerTabRowKey, dragState = WorkbenchDragController.dragState) {
                    WorkbenchTabRow(informationState, leftExplorerTabRowKey)
                }
                WorkbenchVerticalSplitPane(splitPaneState = informationState.bottomSplitState) {
                    first {
                        WorkbenchHorizontalSplitPane(splitPaneState = informationState.leftSplitState) {
                            first {
                                WorkbenchTabBody(informationState, leftExplorerTabRowKey)
                            }
                            second {
                                WorkbenchEditorSpace(informationState)
                            }
                        }
                    }
                    second {
                        WorkbenchTabBody(informationState, bottomExplorerTabRowKey)
                    }
                }
            }
            Row(
                modifier = Modifier.weight(if (hasBottomTabs) 0.1f else 0.001f)
                    .padding(start = TAB_ROW_WIDTH.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                DropTarget(tabRowKey = bottomExplorerTabRowKey, dragState = WorkbenchDragController.dragState) {
                    WorkbenchTabRow(informationState, bottomExplorerTabRowKey)
                }
            }
        }
    }
}