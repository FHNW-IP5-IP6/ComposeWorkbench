package controller

import TAB_ROW_HEIGHT
import TAB_ROW_WIDTH
import androidx.compose.runtime.MutableState
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

internal class WorkbenchModuleController(val model: WorkbenchModel, val displayType: DisplayType, val moduleType: ModuleType, val deselectable: Boolean = false) {

    init {
        if(getModulesFiltered().isEmpty()) model.hideDrawer(displayType)
    }

    fun getScrollToIndex(): Int{
        if(model.previewModule != null && model.previewModule!!.displayType == displayType){
            return getIndex(model.previewModule)
        }
        return getIndex(getSelectedModule().value)
    }

    fun getIndex(explorer: WorkbenchModuleState<*>?): Int {
        val index = getModulesFiltered().indexOfFirst { it.id == explorer?.id }
        return index.coerceAtLeast(0)
    }

    fun getSelectedModule(): MutableState<WorkbenchModuleState<*>?> {
        return model.getSelectedModule(displayType, moduleType)
    }

    fun getTabRowMinDimension(): Pair<Dp, Dp> {
        if(getModulesFiltered().isNotEmpty()) {
            return Pair(TAB_ROW_WIDTH, TAB_ROW_HEIGHT)
        }
        return Pair(4.dp, 4.dp)
    }

    fun getModulesFiltered(): List<WorkbenchModuleState<*>> {
        return model.modules.filter { it.displayType== displayType && it.module.moduleType == moduleType }.reversed()
    }

    fun moduleSelectorPressed(module: WorkbenchModuleState<*>?) {
        if(deselectable && getSelectedModule().value == module){
            model.setSelectedModuleNull(displayType, moduleType)
        }else{
            model.setSelectedModule(module!!)
        }
    }

    fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean {
        return getSelectedModule().value != null && getSelectedModule().value == module
    }

    fun removeModuleState(module: WorkbenchModuleState<*>){
        model.removeTab(module)
    }

    fun <M> convertToWindow(module: WorkbenchModuleState<M>) {
        model.moduleToWindow(module)
    }

    internal fun updateDisplayType(module: WorkbenchModuleState<*>, displayType: DisplayType){
        if(displayType != module.displayType){
            model.switchDisplayType(module, displayType)
        }else if(!isModuleSelected(module)) {
            moduleSelectorPressed(module)
        }
    }
}