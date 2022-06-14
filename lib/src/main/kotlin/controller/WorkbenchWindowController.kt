package controller

import model.WorkbenchModel
import model.data.DisplayType
import model.data.ModuleType
import model.state.PreviewState
import model.state.WindowStateAware
import model.state.WorkbenchModuleState

internal class WorkbenchWindowController(
    override val model: WorkbenchModel,
    val windowState: WindowStateAware,
    override val previewState: PreviewState = PreviewState()
): WorkbenchController {

    override val displayType = DisplayType.WINDOW
    override val moduleType: ModuleType
        get() = windowState.modules[0].module.moduleType

    override fun getWindow(): WindowStateAware = windowState

    override fun getSelectedModule(): WorkbenchModuleState<*>? {
        return windowState.selectedModule
    }

    override fun getModulesFiltered(): List<WorkbenchModuleState<*>> {
        return windowState.modules.reversed()
    }

    override fun moduleSelectorPressed(module: WorkbenchModuleState<*>?) {
        windowState.selectedModule = module
    }

    override fun isModuleSelected(module: WorkbenchModuleState<*>?): Boolean {
        return getSelectedModule() == module
    }

    override fun removeModuleState(module: WorkbenchModuleState<*>){
        windowState.modules -= module
    }

    override fun onModuleDraggedOut(module: WorkbenchModuleState<*>) {
        removeModule(module = module)
    }

    override fun onModuleDroppedIn(module: WorkbenchModuleState<*>) {
        model.windows -= windowState
        module.displayType = DisplayType.WINDOW
        windowState.selectedModule = module
        windowState.modules += module
        model.windows += windowState
    }

    private fun removeModule(module: WorkbenchModuleState<*>){
        model.windows -= windowState
        windowState.modules -= module
        if (windowState.modules.isNotEmpty()) {
            model.windows += windowState
        }
    }
}