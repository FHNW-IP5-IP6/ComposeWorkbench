package model.state

import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.SplitViewMode
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.jetbrains.compose.splitpane.SplitPaneState

/**
 * Immutable State which holds all display relevant Data that is subject to changes
 */
internal data class WorkbenchInformationState(
    val modules: List<WorkbenchModuleState<*>>,
    val windows: List<WorkbenchWindowState>,
    val tabRowState: Map<TabRowKey, WorkbenchTabRowState>,
    val unsavedEditors: Map<String, MutableSet<Int>>,
    val splitViewMode: SplitViewMode,
    val currentTabSpace: DisplayType,
    val bottomSplitState: SplitPaneState,
    val leftSplitState: SplitPaneState
) {

}

@OptIn(ExperimentalSplitPaneApi::class)
internal fun getDefaultWorkbenchDisplayInformation(): WorkbenchInformationState {
    return WorkbenchInformationState(
        modules = listOf(),
        windows = listOf(),
        tabRowState = mapOf(),
        unsavedEditors = mapOf(),
        splitViewMode = SplitViewMode.UNSPLIT,
        currentTabSpace = DisplayType.TAB1,
        bottomSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f),
        leftSplitState =  SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f)
    )
}
