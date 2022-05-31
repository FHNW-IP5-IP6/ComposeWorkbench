package view.conponent

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
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
                        model = model,
                        displayType = DisplayType.TAB1,
                        controller = editorTabController1
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        model = model,
                        displayType = DisplayType.TAB2,
                        controller = editorTabController2,
                    )
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            WorkbenchHorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    EditorSpaceDropTarget(
                        model = model,
                        displayType = DisplayType.TAB1,
                        controller = editorTabController1
                    )
                }
                second {
                    EditorSpaceDropTarget(
                        model = model,
                        displayType = DisplayType.TAB2,
                        controller = editorTabController2
                    )
                }
            }
        }
        else -> {
            TabSpace(editorTabController1)
        }
    }
}

@Composable
private fun EditorSpaceDropTarget(
    model: WorkbenchModel,
    controller: WorkbenchModuleController,
    displayType: DisplayType
){
    DropTarget(
        model = model,
        dropTargetType = displayType,
        moduleReceiver = {controller.updateDisplayType(it, displayType)},
        acceptedType = ModuleType.EDITOR
    ) {
        TabSpace(controller)
    }
}

@Composable
private fun TabSpace(controller: WorkbenchModuleController){
    Column {
        WorkbenchTabRow(controller)
        WorkbenchTabBody(controller)
    }
}
