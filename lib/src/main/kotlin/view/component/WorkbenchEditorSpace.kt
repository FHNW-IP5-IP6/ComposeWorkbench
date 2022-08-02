package view.component

import androidx.compose.runtime.Composable
import controller.Action
import controller.WorkbenchAction
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.state.WorkbenchDragState
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

/**
 * Component which shows all currently opened Editors
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchEditorSpace(
    informationState: WorkbenchInformationState,
    dragState: WorkbenchDragState,
    onActionRequired: (Action) -> Unit,
){
    val editorTabRowKey1 = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = informationState.mainWindow)
    val editorTabRowKey2 = TabRowKey(displayType = DisplayType.TAB2, moduleType = ModuleType.EDITOR, windowState = informationState.mainWindow)
    val splitRatio = .5f

    when (informationState.splitViewMode) {
        SplitViewMode.VERTICAL -> {
            onActionRequired.invoke(WorkbenchAction.VerifySplitViewMode(editorTabRowKey1, editorTabRowKey2))
            WorkbenchVerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        informationState = informationState,
                        dragState = dragState,
                        onActionRequired = onActionRequired,
                        tabRowKey = editorTabRowKey1,
                        onSelect =  { onActionRequired.invoke(WorkbenchAction.UpdateCurrentTabSpace(DisplayType.TAB1)) }
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        informationState = informationState,
                        dragState = dragState,
                        onActionRequired = onActionRequired,
                        tabRowKey = editorTabRowKey2,
                        onSelect =  {onActionRequired.invoke(WorkbenchAction.UpdateCurrentTabSpace(DisplayType.TAB2)) }
                    )
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            onActionRequired.invoke(WorkbenchAction.VerifySplitViewMode(editorTabRowKey1, editorTabRowKey2))
            WorkbenchHorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        informationState = informationState,
                        dragState = dragState,
                        onActionRequired = onActionRequired,
                        tabRowKey = editorTabRowKey1,
                        onSelect =  {onActionRequired.invoke(WorkbenchAction.UpdateCurrentTabSpace(DisplayType.TAB1)) }
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        informationState = informationState,
                        dragState = dragState,
                        onActionRequired = onActionRequired,
                        tabRowKey = editorTabRowKey2,
                        onSelect =  {onActionRequired.invoke(WorkbenchAction.UpdateCurrentTabSpace(DisplayType.TAB2)) }
                    )
                }
            }
        }
        else -> {
            EditorSpaceDropTarget(
                informationState = informationState,
                dragState = dragState,
                onActionRequired = onActionRequired,
                tabRowKey = editorTabRowKey1,
                onSelect =  {onActionRequired.invoke(WorkbenchAction.UpdateCurrentTabSpace(DisplayType.TAB1)) }
            )
        }
    }
}

@Composable
private fun EditorSpaceDropTarget(
    informationState: WorkbenchInformationState,
    dragState: WorkbenchDragState,
    onActionRequired: (Action) -> Unit,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit
){
    DropTarget(informationState = informationState, onActionRequired = onActionRequired, dragState = dragState, tabRowKey = tabRowKey) {
        TabSpace(informationState, dragState, onActionRequired, tabRowKey, onSelect)
    }
}
