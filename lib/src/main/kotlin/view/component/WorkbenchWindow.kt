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
import model.state.WorkbenchInformationState

@Composable
internal fun WorkbenchWindow(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit,
    workbenchState: WorkbenchState
){
    if (workbenchState == WorkbenchState.RUNNING) {
        for (state in informationState.windows) {
            key(state) {
                println("window")
                val tabRowKey =
                    TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = state)
                DragAndDropWindow(
                    informationState = informationState,
                    onActionRequired = onActionRequired,
                    onCloseRequest = {
                        onActionRequired.invoke(WorkbenchAction.CloseAll(tabRowKey.windowState))
                        onActionRequired.invoke(WorkbenchAction.RemoveWindow(tabRowKey))
                    },
                    tabRowKey = tabRowKey,
                ) {
                    Column {
                        DropTarget(
                            tabRowKey = tabRowKey,
                            onActionRequired = onActionRequired
                        ) {
                            WorkbenchTabRow(informationState, onActionRequired, tabRowKey)
                        }
                        WorkbenchTabBody(informationState, onActionRequired, tabRowKey)
                    }
                }
            }
        }
    }
}