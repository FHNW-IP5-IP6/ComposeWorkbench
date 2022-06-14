package controller

import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.state.PreviewState
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

internal class WorkbenchModuleController(
    override val model: WorkbenchModel,
    override val displayType: DisplayType,
    override val moduleType: ModuleType,
    private val deselectable: Boolean = false,
    override val previewState: PreviewState = PreviewState()
): WorkbenchController {

    init {
        if(getModulesFiltered().isEmpty()) model.hideDrawer(displayType)
    }

    override fun getSelectedModule(): WorkbenchModuleState<*>? {
        return model.getSelectedModule(displayType, moduleType).value
    }

    override fun getModulesFiltered(): List<WorkbenchModuleState<*>> {
        return model.modules.filter { it.displayType== displayType && it.module.moduleType == moduleType }.reversed()
    }

    override fun moduleSelectorPressed(module: WorkbenchModuleState<*>?) {
        if(deselectable && getSelectedModule() == module){
            model.setSelectedModuleNull(displayType, moduleType)
        }else{
            model.setSelectedModule(module!!)
        }
    }

    override fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean {
        return getSelectedModule() != null && getSelectedModule() == module
    }

    override fun removeModuleState(module: WorkbenchModuleState<*>){
        model.removeTab(module)
    }

    override fun getWindow(): WindowStateAware = model.mainWindow

    override fun onModuleDraggedOut(module: WorkbenchModuleState<*>) {
        removeModuleState(module)
    }

    override fun onModuleDroppedIn(module: WorkbenchModuleState<*>) {
        if(displayType != module.displayType){
            model.switchDisplayType(module, displayType)
        }else if(!isModuleSelected(module)) {
            moduleSelectorPressed(module)
        }
    }
}