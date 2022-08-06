package controller

import model.data.Command
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.SplitViewMode
import model.state.PopUpState
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState

internal sealed class WorkbenchAction(
    val name: String): Action {

    class InitExplorers                                                                                 : WorkbenchAction("Init explorers")
    class SaveAll                                                                                       : WorkbenchAction("Save all")
    class DropDraggedModule                                                                             : WorkbenchAction("Drop dragged module")

    class RemovePopUp(val tabRowKey: TabRowKey)                                                         : WorkbenchAction("Remove popUp")
    class SetPopUp(val tabRowKey: TabRowKey, val popUpState: PopUpState)                                : WorkbenchAction("Set popUp")

    class UpdateCurrentTabSpace(val displayType: DisplayType)                                           : WorkbenchAction("Update current tab space")
    class HideDrawer(val displayType: DisplayType)                                                      : WorkbenchAction("Hide drawer")
    class ShowDrawer(val displayType: DisplayType)                                                      : WorkbenchAction("Show drawer")
    class RemoveWindow(val tabRowKey: TabRowKey)                                                        : WorkbenchAction("Remove window")

    class AddUnsavedModule(val type: String, val dataId: Int)                                           : WorkbenchAction("Add unsaved module")
    class RemoveSavedModule(val type: String, val dataId: Int)                                          : WorkbenchAction("Remove saved module")
    class RequestEditorState(val type: String, val dataId: Int)                                         : WorkbenchAction("Request editor state")

    class CloseModuleState(val moduleState: WorkbenchModuleState<*>)                                    : WorkbenchAction("Close module state")
    class ReselectModuleState(val moduleState: WorkbenchModuleState<*>)                                 : WorkbenchAction("Reselect module state")
    class ModuleToWindow(val moduleState: WorkbenchModuleState<*>)                                      : WorkbenchAction("Module to window")
    class AddModuleState(val moduleState: WorkbenchModuleState<*>)                                      : WorkbenchAction("Add module state")
    class RemoveModuleState(val moduleState: WorkbenchModuleState<*>)                                   : WorkbenchAction("Remove module state")
    class SaveModuleState(val moduleState: WorkbenchModuleState<*>, val action: () -> Unit)             : WorkbenchAction("Save module state")
    class UpdateEditor(val moduleState: WorkbenchModuleState<*>, val module: WorkbenchModule<*>)    : WorkbenchAction("Update module state module")


    class VerifySplitViewMode(val tabRowKey1: TabRowKey, val tabRowKey2: TabRowKey)                     : WorkbenchAction("Verify split view mode")
    class UpdateSelection(val tabRowKey: TabRowKey, val moduleState: WorkbenchModuleState<*>?)          : WorkbenchAction("Update selection")
    class TabSelectorPressed(val tabRowKey: TabRowKey,val moduleState: WorkbenchModuleState<*>)         : WorkbenchAction("Tab selector pressed")

    class RegisterEditor(val moduleType: String, val editor: WorkbenchModule<*>)                        : WorkbenchAction("Register editor")
    class RegisterExplorer(val moduleType: String, val explorer: WorkbenchModule<*>)                    : WorkbenchAction("Register explorer")

    class ChangeSplitViewMode(val splitViewMode: SplitViewMode)                                         : WorkbenchAction("Change split view mode")
    class AddDefaultExplorer(val id: Int, val state: WorkbenchDefaultState<*>)         : WorkbenchAction("Add default explorer")
    class AddCommand(val command: Command)                                                              : WorkbenchAction("Add command")

    class CreateExplorerFromDefault(val id: Int)                                                        : WorkbenchAction("Create explorer from default")
    class SetAppTitle(val appTitle: String)                                                             : WorkbenchAction("Set app title")
    class RequestExplorerState(val moduleState: WorkbenchModuleState<*>)                                : WorkbenchAction("Request explorer state")
}
