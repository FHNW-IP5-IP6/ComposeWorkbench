package controller

import androidx.compose.runtime.MutableState
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.data.SplitViewMode
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.SplitPaneState

internal class WorkbenchSelectionController(private val model: WorkbenchModel) {

    fun getSelectedModule (displayType: DisplayType, moduleType: ModuleType): MutableState<WorkbenchModuleState<*>?> {
        return model.selectedModules[displayType.ordinal][moduleType.ordinal]
    }

    fun setSelectedModuleNull (displayType: DisplayType, moduleType: ModuleType) {
        model.selectedModules[displayType.ordinal][moduleType.ordinal].value = null
        hideDrawer(displayType)
    }

    fun reselectState(state: WorkbenchModuleState<*>) {
        val selected = getSelectedModule(state.displayType, state.module.moduleType)
        val filteredStates = model.modules.filter { it.displayType == state.displayType && it.module.moduleType == state.module.moduleType }.reversed()
        if (filteredStates.size <= 1) {
            setSelectedModuleNull(state.displayType, state.module.moduleType)
        } else if(selected.value == null || selected.value!!.id == state.id){
            when (val idx = filteredStates.indexOfFirst { it.id == state.id}.coerceAtLeast(0)) {
                0 -> {
                    setSelectedModule(filteredStates[1])
                }
                else -> {
                    setSelectedModule(filteredStates[idx - 1])
                }
            }
        }
    }

    fun setSelectedModule (state: WorkbenchModuleState<*>) {
        model.selectedModules[state.displayType.ordinal][state.module.moduleType.ordinal].value = state
        showDrawer(state.displayType)
        if (state.displayType == DisplayType.TAB1 || state.displayType == DisplayType.TAB2)
            model.currentTabSpace = state.displayType
    }

    fun switchDisplayType(state: WorkbenchModuleState<*>, displayType: DisplayType) {
        updateModuleState(state) {it.displayType = displayType}
        reselectEditorSpace()
    }

    fun updateModuleState(state: WorkbenchModuleState<*>, updater: (WorkbenchModuleState<*>) -> Unit){
        model.modules.remove(state)
        updater.invoke(state)
        addModuleState(state)
    }

    fun removeTab(tab: WorkbenchModuleState<*>) {
        reselectState(tab)
        model.modules.remove(tab)
        reselectEditorSpace()
    }

    fun addModuleState(state: WorkbenchModuleState<*>) {
        if (model.modules.any{ state.model == it.model && !it.isPreview }) return
        model.modules += state
        setSelectedModule(state)
    }

    fun hideDrawer(displayType: DisplayType) {
        when(displayType) {
            DisplayType.LEFT -> model.leftSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 0f)
            DisplayType.BOTTOM -> model.bottomSplitState = SplitPaneState(moveEnabled = false, initialPositionPercentage = 1f)
            else -> Unit
        }
    }

    private fun showDrawer(displayType: DisplayType) {
        when(displayType) {
            DisplayType.LEFT -> model.leftSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.25f)
            DisplayType.BOTTOM -> model.bottomSplitState = SplitPaneState(moveEnabled = true, initialPositionPercentage = 0.7f)
            else -> Unit
        }
    }

    //Merge Editor space in case one of the individual spaces is empty
    private fun reselectEditorSpace() {
        val tabs1 = model.modules.filter { it.displayType == DisplayType.TAB1 }
        val tabs2 = model.modules.filter { it.displayType == DisplayType.TAB2 }
        if (tabs1.isEmpty() || tabs2.isEmpty()) {
            changeSplitViewMode(SplitViewMode.UNSPLIT)
        }
    }

    fun changeSplitViewMode (mode: SplitViewMode) {
        if (mode == model.splitViewMode) return

        model.splitViewMode = mode

        val tabs1 = model.modules.filter { it.displayType == DisplayType.TAB1 }
        val tabs2 = model.modules.filter { it.displayType == DisplayType.TAB2 }

        if (mode == SplitViewMode.UNSPLIT) {
            val m2 = getSelectedModule(DisplayType.TAB2, ModuleType.EDITOR).value
            if (m2 != null) {
                reselectState(m2)
                m2.displayType = DisplayType.TAB1
                setSelectedModule(m2)
                setSelectedModuleNull(DisplayType.TAB2, ModuleType.EDITOR)
            }
            tabs2.forEach { it.displayType = DisplayType.TAB1 }
            model.currentTabSpace = DisplayType.TAB1
            return
        }

        if (tabs1.isEmpty() && tabs2.size==1 || tabs1.size==1 && tabs2.isEmpty()) return

        val m1 = getSelectedModule(DisplayType.TAB1, ModuleType.EDITOR).value
        val m2 = getSelectedModule(DisplayType.TAB2, ModuleType.EDITOR).value
        if (m1 != null && m2 == null) {
            reselectState(m1)
            m1.displayType = DisplayType.TAB2
            setSelectedModule(m1)
        }
        if (m1 == null && m2 != null) {
            reselectState(m2)
            m2.displayType = DisplayType.TAB1
            setSelectedModule(m2)
        }
    }
}