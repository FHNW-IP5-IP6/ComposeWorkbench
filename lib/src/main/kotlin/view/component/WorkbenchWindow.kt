package view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import controller.Action
import controller.WorkbenchAction
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.WorkbenchState
import model.state.WorkbenchDragState
import model.state.WorkbenchInformationState

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
                println("recompose window")
                val tabRowKey =
                    TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = state)
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