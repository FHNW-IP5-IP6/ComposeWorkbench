package view.conponent

import SPLIT_PAIN_HANDLE_ALPHA
import SPLIT_PAIN_HANDLE_AREA
import SPLIT_PAIN_HANDLE_SIZE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.data.SplitViewMode
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import util.cursorForHorizontalResize
import util.cursorForVerticalResize

/**
 * Component which shows all currently opened Editors
 */
@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun EditorTabSpace(model: WorkbenchModel){
    val editorTabController1 = WorkbenchModuleController(model, DisplayType.TAB1, ModuleType.EDITOR)
    val editorTabController2 = WorkbenchModuleController(model, DisplayType.TAB2, ModuleType.EDITOR)
    var splitRatio = .5f
    if (editorTabController1.getModulesFiltered().isEmpty()) splitRatio = 0f
    if (editorTabController2.getModulesFiltered().isEmpty()) splitRatio = 1f

    when (model.splitViewMode) {
        SplitViewMode.VERTICAL -> {
            VerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    DropTarget(
                        model = model,
                        dropTargetType = DisplayType.TAB1,
                        moduleReceiver = {editorTabController1.updateDisplayType(it, DisplayType.TAB1)},
                        acceptedType = ModuleType.EDITOR
                    ){
                        TabSpace(editorTabController1)
                    }
                }
                second {
                    DropTarget(
                        model = model,
                        dropTargetType = DisplayType.TAB2,
                        moduleReceiver = { editorTabController2.updateDisplayType(it, DisplayType.TAB2) },
                        acceptedType = ModuleType.EDITOR
                    ) {
                        TabSpace(editorTabController2)
                    }
                }
                splitter {
                    visiblePart {
                        Box(modifier = Modifier.height(SPLIT_PAIN_HANDLE_SIZE).fillMaxWidth().background(SolidColor(Color.Gray), alpha = SPLIT_PAIN_HANDLE_ALPHA))
                    }
                    handle {
                        Box(modifier = Modifier.markAsHandle().cursorForVerticalResize().height(SPLIT_PAIN_HANDLE_AREA).fillMaxWidth())
                    }
                }
            }
        }
        SplitViewMode.HORIZONTAL -> {
            HorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
                first {
                    DropTarget(
                        model = model,
                        dropTargetType = DisplayType.TAB1,
                        moduleReceiver = {editorTabController1.updateDisplayType(it, DisplayType.TAB1)},
                        acceptedType = ModuleType.EDITOR
                    ) {
                        TabSpace(editorTabController1)
                    }
                }
                second {
                    DropTarget(
                        model = model,
                        dropTargetType = DisplayType.TAB2,
                        moduleReceiver = {editorTabController2.updateDisplayType(it, DisplayType.TAB2)},
                        acceptedType = ModuleType.EDITOR
                    ) {
                        TabSpace(editorTabController2)
                    }
                }
                splitter {
                    visiblePart {
                        Box(modifier = Modifier.width(SPLIT_PAIN_HANDLE_SIZE).fillMaxHeight().background(SolidColor(Color.Gray), alpha = SPLIT_PAIN_HANDLE_ALPHA))
                    }
                    handle {
                        Box(modifier = Modifier.markAsHandle().cursorForHorizontalResize().width(SPLIT_PAIN_HANDLE_AREA).fillMaxHeight())
                    }
                }
            }
        }
        else -> {
            TabSpace(editorTabController1)
        }
    }
}

@Composable
private fun TabSpace(controller: WorkbenchModuleController){
    Column {
        WorkbenchTabRow(controller)
        WorkbenchTabBody(controller)
    }
}
