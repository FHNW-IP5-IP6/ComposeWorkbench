package view.component

import androidx.compose.runtime.Composable
import controller.WorkbenchController
import controller.WorkbenchDisplayController
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

/**
 * Component which shows all currently opened Editors
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchEditorSpace(controller: WorkbenchController){
    val editorTabController1 = controller.getDisplayController(DisplayType.TAB1, ModuleType.EDITOR, false)
    val editorTabController2 = controller.getDisplayController(DisplayType.TAB2, ModuleType.EDITOR, false)
    var splitRatio = .5f
    if (editorTabController1.getModulesFiltered().isEmpty()) splitRatio = 0f
    if (editorTabController2.getModulesFiltered().isEmpty()) splitRatio = 1f

    when (controller.getSplitViewMode()) {
        SplitViewMode.VERTICAL -> {
            WorkbenchVerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        displayController = editorTabController1,
                        controller = controller
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        displayController = editorTabController2,
                        controller = controller
                    )
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            WorkbenchHorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        displayController = editorTabController1,
                        controller = controller
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        displayController = editorTabController2,
                        controller = controller
                    )
                }
            }
        }
        else -> {
            EditorSpaceDropTarget(
                displayController = editorTabController1,
                controller = controller
            )
        }
    }
}

@Composable
private fun EditorSpaceDropTarget(
    displayController: WorkbenchDisplayController,
    controller: WorkbenchController
){
    DropTarget(controller = displayController) {
        TabSpace(displayController, controller)
    }
}
