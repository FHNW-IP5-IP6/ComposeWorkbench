package view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.example.ui.theme.NotoSansTypography
import controller.Action
import controller.WorkbenchAction
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.WorkbenchState
import model.state.WorkbenchDragState
import model.state.WorkbenchInformationState
import view.themes.LightColors

@Composable
internal fun WorkbenchWindow(
    informationState: WorkbenchInformationState,
    dragState: WorkbenchDragState,
    onActionRequired: (Action) -> Unit,
    workbenchState: WorkbenchState
){
    if (workbenchState == WorkbenchState.RUNNING) {
        key(informationState.windows) {
            for (state in informationState.windows) {
                val tabRowKey =
                    TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = state)
                if (informationState.getModulesFiltered(tabRowKey).isEmpty()) {
                    onActionRequired.invoke(WorkbenchAction.RemoveWindow(tabRowKey))
                    continue
                }
                DragAndDropWindow(
                    informationState = informationState,
                    onActionRequired =  onActionRequired,
                    onCloseRequest = {
                        informationState.getModulesFiltered(tabRowKey).forEach { it.module.onClose }
                        onActionRequired.invoke(WorkbenchAction.RemoveWindow(tabRowKey))
                    },
                    tabRowKey = tabRowKey,
                    dragState = dragState
                ) {
                    MaterialTheme(
                        colors = LightColors,
                        typography = NotoSansTypography,
                    ) {
                        Column {
                            DropTarget(informationState = informationState, tabRowKey = tabRowKey, dragState = dragState, onActionRequired =  onActionRequired) {
                                WorkbenchTabRow(informationState, dragState, onActionRequired, tabRowKey)
                            }
                            WorkbenchTabBody(informationState, onActionRequired, tabRowKey)
                        }
                    }
                }
            }
        }
    }
}