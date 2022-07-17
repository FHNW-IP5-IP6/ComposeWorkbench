package model.data

import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState

internal class TabRowKey(
    val displayType: DisplayType,
    val moduleType: ModuleType,
    val windowState: WorkbenchWindowState
) {
    constructor(moduleState: WorkbenchModuleState<*>) :
            this(
                moduleState.displayType,
                if (DisplayType.WINDOW == moduleState.displayType) ModuleType.BOTH else moduleState.module.moduleType,
                moduleState.window
            )

    override fun equals(other: Any?): Boolean = (other is TabRowKey)
            && displayType == other.displayType
            && moduleType == other.moduleType
            && windowState == other.windowState

    override fun hashCode(): Int {
        var result = displayType.hashCode()
        result = 31 * result + moduleType.hashCode()
        result = 31 * result + windowState.hashCode()
        return result
    }

    override fun toString(): String = "TabRowKey: displayType: $displayType, moduleType $moduleType, window ${windowState.windowState.position}"
}