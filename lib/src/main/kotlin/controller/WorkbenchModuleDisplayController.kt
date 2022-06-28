package controller

import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.state.PreviewState
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

internal class WorkbenchModuleDisplayController(
    override val model: WorkbenchModel,
    override val displayType: DisplayType,
    override val moduleType: ModuleType,
    private val deselectable: Boolean = false,
    override val selectionController: WorkbenchSelectionController,
    override val previewState: PreviewState = PreviewState()
): WorkbenchDisplayController {

    init {
        if(getModulesFiltered().isEmpty()) selectionController.hideDrawer(displayType)
    }

    override fun getSelectedModule(): WorkbenchModuleState<*>? {
        return selectionController.getSelectedModule(displayType, moduleType).value
    }

    override fun getModulesFiltered(): List<WorkbenchModuleState<*>> {
        return model.modules.filter { it.displayType== displayType && it.module.moduleType == moduleType }.reversed()
    }

    override fun moduleSelectorPressed(module: WorkbenchModuleState<*>?) {
        if(deselectable && getSelectedModule() == module){
            selectionController.setSelectedModuleNull(displayType, moduleType)
        }else{
            selectionController.setSelectedModule(module!!)
        }
    }

    override fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean {
        return getSelectedModule() != null && getSelectedModule() == module
    }

    override fun removeModuleState(module: WorkbenchModuleState<*>){
        selectionController.removeTab(module)
    }

    override fun getWindow(): WindowStateAware = model.mainWindow

    override fun onModuleDraggedOut(module: WorkbenchModuleState<*>) {
        removeModuleState(module)
    }

    override fun onModuleDroppedIn(module: WorkbenchModuleState<*>) {
        if(displayType != module.displayType){
            selectionController.switchDisplayType(module, displayType)
        }else if(!isModuleSelected(module)) {
            moduleSelectorPressed(module)
        }
    }
}