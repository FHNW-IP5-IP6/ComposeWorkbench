package view.component

import androidx.compose.runtime.Composable
import controller.WorkbenchController
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.data.SplitViewMode
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.rememberSplitPaneState

/**
 * Component which shows all currently opened Editors
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun WorkbenchEditorSpace(model: WorkbenchModel){
    val editorTabController1 = WorkbenchModuleController(model, DisplayType.TAB1, ModuleType.EDITOR)
    val editorTabController2 = WorkbenchModuleController(model, DisplayType.TAB2, ModuleType.EDITOR)
    var splitRatio = .5f
    if (editorTabController1.getModulesFiltered().isEmpty()) splitRatio = 0f
    if (editorTabController2.getModulesFiltered().isEmpty()) splitRatio = 1f

    when (model.splitViewMode) {
        SplitViewMode.VERTICAL -> {
            WorkbenchVerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        controller = editorTabController1
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        controller = editorTabController2,
                    )
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            WorkbenchHorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        controller = editorTabController1
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        controller = editorTabController2
                    )
                }
            }
        }
        else -> {
            EditorSpaceDropTarget(
                controller = editorTabController1
            )
        }
    }
}

@Composable
private fun EditorSpaceDropTarget(
    controller: WorkbenchController
){
    DropTarget(controller = controller) {
        TabSpace(controller)
    }
}
