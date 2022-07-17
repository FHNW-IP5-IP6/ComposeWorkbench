package view.component

import androidx.compose.runtime.Composable
import controller.WorkbenchController
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

/**
 * Component which shows all currently opened Editors
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchEditorSpace(controller: WorkbenchController){
    val editorTabRowKey1 = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = controller.getMainWindow())
    val editorTabRowKey2 = TabRowKey(displayType = DisplayType.TAB2, moduleType = ModuleType.EDITOR, windowState = controller.getMainWindow())
    var splitRatio = .5f

    when (controller.informationState.splitViewMode) {
        SplitViewMode.VERTICAL -> {
            controller.verifySplitViewMde(editorTabRowKey1, editorTabRowKey2)
            WorkbenchVerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey1,
                        controller = controller,
                        onSelect =  {controller.updateCurrentTabSpace(DisplayType.TAB1)}
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey2,
                        controller = controller,
                        onSelect =  {controller.updateCurrentTabSpace(DisplayType.TAB2)}
                    )
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            controller.verifySplitViewMde(editorTabRowKey1, editorTabRowKey2)
            WorkbenchHorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey1,
                        controller = controller,
                        onSelect =  {controller.updateCurrentTabSpace(DisplayType.TAB1)}
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        tabRowKey = editorTabRowKey2,
                        controller = controller,
                        onSelect =  {controller.updateCurrentTabSpace(DisplayType.TAB2)}
                    )
                }
            }
        }
        else -> {
            EditorSpaceDropTarget(
                tabRowKey = editorTabRowKey1,
                controller = controller,
                onSelect =  {controller.updateCurrentTabSpace(DisplayType.TAB1)}
            )
        }
    }
}

@Composable
private fun EditorSpaceDropTarget(
    tabRowKey: TabRowKey,
    controller: WorkbenchController,
    onSelect: (WorkbenchModuleState<*>) -> Unit
){
    DropTarget(tabRowKey = tabRowKey, controller = controller) {
        TabSpace(tabRowKey, controller, onSelect)
    }
}
