package model.state

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.window.WindowPosition
import model.data.TabRowKey
import model.data.enums.ModuleType
import util.toOffset

/**
 * Immutable state which holds information about the currently dragged module and its position
 */
internal data class WorkbenchDragState(
    val isDragging: Boolean,
    val module: WorkbenchModuleState<*>?,
    val positionOnScreen: DpOffset,
    val dropTargets: List<DropTarget>
){
    internal fun isCurrentDropTarget(tabRowKey: TabRowKey): Boolean {
        val reverseDropTarget = getCurrentReverseDopTarget() ?: return false //If there is no reverse target active the drop target is outside any window
        val dropTarget = getCurrentDopTarget(reverseDropTarget.tabRowKey.windowState)
        return  dropTarget != null && dropTarget.tabRowKey == tabRowKey
    }

    internal fun getCurrentReverseDopTarget(): DropTarget? {
        if(positionOnScreen.isUnspecified) return null
        val targets = dropTargets.filter { it.isReverse && it.bounds.contains(
            positionOnScreen.toOffset()) }
        return when (targets.isEmpty()){
            true -> null
            false -> {
                //Prioritize the window witch is currently focused
                if (targets.find { it.tabRowKey.windowState.hasFocus } == null) targets.first() else targets.find { it.tabRowKey.windowState.hasFocus }!!
            }
        }
    }

    internal fun toOffset(windowState: WorkbenchWindowState): Offset {
        val x = positionOnScreen.x - windowState.windowState.position.x
        val y = positionOnScreen.y - windowState.windowState.position.y
        return Offset(x.value,y.value)
    }

    internal fun getWindowPosition(): WindowPosition {
        if(positionOnScreen.isUnspecified) return WindowPosition.PlatformDefault
        return WindowPosition(x = positionOnScreen.x, y = positionOnScreen.y)
    }

    internal fun getCurrentDopTarget(windowState: WorkbenchWindowState): DropTarget? {
        if(positionOnScreen.isUnspecified) return null
        return dropTargets.find { !it.isReverse && it.bounds.contains(
            positionOnScreen.toOffset()) && it.tabRowKey.windowState == windowState }
    }

    internal fun isValidDropTarget(tabRowKey: TabRowKey, informationState: WorkbenchInformationState): Boolean {
        val moduleType = module?.module?.moduleType ?: return false
        return (ModuleType.BOTH == moduleType
                || ModuleType.BOTH == tabRowKey.moduleType
                || moduleType == tabRowKey.moduleType)
                && !informationState.getModulesFiltered(tabRowKey).contains(module)
    }
}

internal data class DropTarget(
    val isReverse: Boolean,
    val tabRowKey: TabRowKey,
    val bounds: Rect
) {
    override fun toString() = "$tabRowKey, isRevers $isReverse, bounds $bounds"
}

internal fun getDefaultWorkbenchDragState(): WorkbenchDragState {
    return WorkbenchDragState(
        isDragging = false,
        positionOnScreen = DpOffset.Zero,
        module = null,
        dropTargets = listOf()
    )
}
