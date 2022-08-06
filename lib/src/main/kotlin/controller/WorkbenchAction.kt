package controller

import kotlinx.coroutines.CompletableDeferred
import model.data.Command
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.PopUpType
import model.data.enums.SplitViewMode
import model.state.PopUpState
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState

internal sealed class WorkbenchAction(
    val name: String
): Action {

    class InitExplorers                                                                                 : WorkbenchAction("Init explorers")
    class SaveAll                                                                                       : WorkbenchAction("Save all")
    class CloseAll(val windowState: WorkbenchWindowState)                                               : WorkbenchAction("Close all")
    class DropDraggedModule                                                                             : WorkbenchAction("Drop dragged module")

    class ClosePopUp()                                                                                  : WorkbenchAction("Close PopUp")
    class OpenPopUp(
        val popUpType: PopUpType,
        val moduleState: WorkbenchModuleState<*>,
        val module: WorkbenchModule<*>,
        val message: String)                                                                            : WorkbenchAction("Open PopUp")

    class UpdateCurrentTabSpace(val displayType: DisplayType)                                           : WorkbenchAction("Update current tab space")
    class HideDrawer(val displayType: DisplayType)                                                      : WorkbenchAction("Hide drawer")
    class ShowDrawer(val displayType: DisplayType)                                                      : WorkbenchAction("Show drawer")
    class RemoveWindow(val tabRowKey: TabRowKey)                                                        : WorkbenchAction("Remove window")

    class AddUnsavedModule(val type: String, val dataId: Int)                                           : WorkbenchAction("Add unsaved module")
    class RemoveSavedModule(val type: String, val dataId: Int)                                          : WorkbenchAction("Remove saved module")

    class CloseModuleState(val moduleState: WorkbenchModuleState<*>)                                    : WorkbenchAction("Close module state")
    class ReselectModuleState(val moduleState: WorkbenchModuleState<*>)                                 : WorkbenchAction("Reselect module state")
    class ModuleToWindow(val moduleState: WorkbenchModuleState<*>)                                      : WorkbenchAction("Module to window")
    class AddModuleState(val moduleState: WorkbenchModuleState<*>)                                      : WorkbenchAction("Add module state")
    class RemoveModuleState(val moduleState: WorkbenchModuleState<*>)                                   : WorkbenchAction("Remove module state")
    class SaveChanges(val moduleState: WorkbenchModuleState<*>)                                         : WorkbenchAction("Save changes of module state")
    class SaveAndClose(val moduleState: WorkbenchModuleState<*>, val popUpState: PopUpState)            : WorkbenchAction("Save changes and close module")
    class DiscardChanges(val moduleState: WorkbenchModuleState<*>, val popUpState: PopUpState)          : WorkbenchAction("Discard changes of module state")
    class UpdateEditor(val moduleState: WorkbenchModuleState<*>, val module: WorkbenchModule<*>)        : WorkbenchAction("Update module state module")


    class VerifySplitViewMode(val tabRowKey1: TabRowKey, val tabRowKey2: TabRowKey)                     : WorkbenchAction("Verify split view mode")
    class UpdateSelection(val tabRowKey: TabRowKey, val moduleState: WorkbenchModuleState<*>?)          : WorkbenchAction("Update selection")
    class TabSelectorPressed(val tabRowKey: TabRowKey,val moduleState: WorkbenchModuleState<*>)         : WorkbenchAction("Tab selector pressed")

    class ChangeSplitViewMode(val splitViewMode: SplitViewMode)                                         : WorkbenchAction("Change split view mode")


    class CreateExplorerFromDefault(val id: Int)                                                        : WorkbenchAction("Create explorer from default")
    class SetAppTitle(val appTitle: String)                                                             : WorkbenchAction("Set app title")
}


internal sealed class WorkbenchActionSync(
    name: String,
    val response: CompletableDeferred<Int> = CompletableDeferred()
): WorkbenchAction(name) {

    class RegisterEditor(val moduleType: String, val editor: WorkbenchModule<*>)                        : WorkbenchActionSync("Register editor")
    class RegisterExplorer(val moduleType: String, val explorer: WorkbenchModule<*>)                    : WorkbenchActionSync("Register explorer")


    class AddDefaultExplorer(val id: Int, val state: WorkbenchDefaultState<*>)                          : WorkbenchActionSync("Add default explorer")
    class AddCommand(val command: Command)                                                              : WorkbenchActionSync("Add command")

    class RequestEditorState(val type: String, val dataId: Int)                                         : WorkbenchActionSync("Request editor state")

    class RequestExplorerState(val moduleState: WorkbenchModuleState<*>)                                : WorkbenchActionSync("Request explorer state")

}
