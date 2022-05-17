package view.conponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import controller.WorkbenchModuleController
import model.WorkbenchModel
import model.data.ModuleType
import model.state.DisplayType
import model.state.SplitViewMode
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.HorizontalSplitPane
import org.jetbrains.compose.splitpane.VerticalSplitPane
import org.jetbrains.compose.splitpane.rememberSplitPaneState
import util.cursorForHorizontalResize
import util.cursorForVerticalResize


@OptIn(ExperimentalSplitPaneApi::class)
@Composable
internal fun EditorTabSpace(model: WorkbenchModel){
    val editorTabController1 = WorkbenchModuleController(model, DisplayType.TAB1, ModuleType.EDITOR)
    val editorTabController2 = WorkbenchModuleController(model, DisplayType.TAB2, ModuleType.EDITOR)
    var splitRatio: Float = .5f
    if (editorTabController1.getModulesFiltered().isEmpty()) splitRatio = 0f
    if (editorTabController2.getModulesFiltered().isEmpty()) splitRatio = 1f

    if (model.splitViewMode == SplitViewMode.VERTICAL) {
        VerticalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
            first {
                TabSpace(editorTabController1)
            }
            second {
                TabSpace(editorTabController2)
            }
            splitter {
                visiblePart {
                    Box(modifier = Modifier.height(2.dp).fillMaxWidth().background(SolidColor(Color.Gray), alpha = 0.50f))
                }
                handle {
                    Box(modifier = Modifier.markAsHandle().cursorForVerticalResize().height(9.dp).fillMaxWidth())
                }
            }
        }
    } else if (model.splitViewMode == SplitViewMode.HORIZONTAL) {
        HorizontalSplitPane(splitPaneState = rememberSplitPaneState(initialPositionPercentage = splitRatio)) {
            first {
                TabSpace(editorTabController1)
            }
            second {
                TabSpace(editorTabController2)
            }
            splitter {
                visiblePart {
                    Box(modifier = Modifier.width(2.dp).fillMaxHeight().background(SolidColor(Color.Gray), alpha = 0.50f))
                }
                handle {
                    Box(modifier = Modifier.markAsHandle().cursorForHorizontalResize().width(9.dp).fillMaxHeight())
                }
            }
        }
    }
    else
    {
        TabSpace(editorTabController1)
    }
}

@Composable
private fun TabSpace(controller: WorkbenchModuleController){
    if (controller.getModulesFiltered().isNotEmpty()) {
        Column {
            WorkbenchTabRow(controller)
            WorkbenchTabBody(controller)
        }
    }else{
        Box{} //empty box for split pane to work
    }
}
