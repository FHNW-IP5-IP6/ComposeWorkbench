package view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController.removeWindow
import controller.WorkbenchDragController
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.WorkbenchState
import model.state.WorkbenchInformationState
import view.themes.LightColors

@Composable
internal fun WorkbenchWindow(
    informationState: WorkbenchInformationState,
    workbenchState: WorkbenchState
){
    if (workbenchState == WorkbenchState.RUNNING) {
        key(informationState.windows) {
            for (state in informationState.windows) {
                val tabRowKey =
                    TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = state)
                if (informationState.getModulesFiltered(tabRowKey).isEmpty()) {
                    removeWindow(tabRowKey)
                    continue
                }
                DragAndDropWindow(
                    informationState = informationState,
                    onCloseRequest = {
                        informationState.getModulesFiltered(tabRowKey).forEach { it.module.onClose }
                        removeWindow(tabRowKey)
                    },
                    tabRowKey = tabRowKey,
                    dragState = WorkbenchDragController.dragState
                ) {
                    MaterialTheme(
                        colors = LightColors,
                        typography = NotoSansTypography,
                    ) {
                        Column {
                            DropTarget(tabRowKey = tabRowKey, dragState = WorkbenchDragController.dragState) {
                                WorkbenchTabRow(informationState, tabRowKey)
                            }
                            WorkbenchTabBody(informationState, tabRowKey)
                        }
                    }
                }
            }
        }
    }
}