package view.component

import androidx.compose.runtime.Composable
import controller.WorkbenchController.updateCurrentTabSpace
import controller.WorkbenchController.verifySplitViewMde
import controller.WorkbenchDragController
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.state.WorkbenchInformationState
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

/**
 * Component which shows all currently opened Editors
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchEditorSpace(informationState: WorkbenchInformationState){
    val editorTabRowKey1 = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = informationState.mainWindow)
    val editorTabRowKey2 = TabRowKey(displayType = DisplayType.TAB2, moduleType = ModuleType.EDITOR, windowState = informationState.mainWindow)
    val splitRatio = .5f

    when (informationState.splitViewMode) {
        SplitViewMode.VERTICAL -> {
            verifySplitViewMde(editorTabRowKey1, editorTabRowKey2)
            WorkbenchVerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey1,
                        informationState = informationState,
                        onSelect =  {updateCurrentTabSpace(DisplayType.TAB1)}
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey2,
                        informationState = informationState,
                        onSelect =  {updateCurrentTabSpace(DisplayType.TAB2)}
                    )
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            verifySplitViewMde(editorTabRowKey1, editorTabRowKey2)
            WorkbenchHorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey1,
                        informationState = informationState,
                        onSelect =  {updateCurrentTabSpace(DisplayType.TAB1)}
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey2,
                        informationState = informationState,
                        onSelect =  {updateCurrentTabSpace(DisplayType.TAB2)}
                    )
                }
            }
        }
        else -> {
            EditorSpaceDropTarget(
                tabRowKey = editorTabRowKey1,
                informationState = informationState,
                onSelect =  {updateCurrentTabSpace(DisplayType.TAB1)}
            )
        }
    }
}

@Composable
private fun EditorSpaceDropTarget(
    informationState: WorkbenchInformationState,
    tabRowKey: TabRowKey,
    onSelect: (WorkbenchModuleState<*>) -> Unit
){
    DropTarget(dragState = WorkbenchDragController.dragState, tabRowKey = tabRowKey) {
        TabSpace(informationState, tabRowKey, onSelect)
    }
}
