package controller

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import model.WorkbenchModel
import model.data.Command
import model.data.MenuEntry
import model.data.MenuItem
import model.data.enums.MenuType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.state.WorkbenchModuleState

@OptIn(ExperimentalComposeUiApi::class)
internal class WorkbenchCommandController(private val model: WorkbenchModel, workbenchController: WorkbenchController) {

    fun getMenuEntry(type: MenuType): MenuEntry {
        return model.commandsMenus[type]!!
    }

    fun getCommandsForType(type: MenuType): MutableList<MenuItem> {
        if (model.commandsMenus[type] != null){
            return model.commandsMenus[type]!!.children
        }
        return mutableListOf()
    }

    init {
        model.commands.addAll(
            listOf(
                Command(text = "Save All",
                    paths = mutableListOf("${MenuType.MenuBar.name}.File"),
                    action = { saveAll(ModuleType.EDITOR) },
                    shortcut = KeyShortcut(Key.S, ctrl = true, alt = true)
                ),
                Command(text = "Horizontal",
                    paths = mutableListOf("${MenuType.MenuBar.name}.View.Split TabSpace"),
                    action = { workbenchController.changeSplitViewMode(SplitViewMode.HORIZONTAL) },
                    shortcut = KeyShortcut(Key.H , ctrl = true, shift = true)
                ),
                Command(text = "Vertical",
                    paths = mutableListOf("${MenuType.MenuBar.name}.View.Split TabSpace"),
                    action = { workbenchController.changeSplitViewMode(SplitViewMode.VERTICAL) },
                    shortcut = KeyShortcut(Key.V , ctrl = true, shift = true)
                ),
                Command(text = "Unsplit",
                    paths = mutableListOf("${MenuType.MenuBar.name}.View.Split TabSpace"),
                    action = { workbenchController.changeSplitViewMode(SplitViewMode.UNSPLIT) },
                    shortcut = KeyShortcut(Key.U , ctrl = true, shift = true)
                ),
            )
        )
    }

    fun dispatchCommands() {
        var m: MenuEntry = model.commandsMenus[MenuType.MenuBar]!!
        for (c in model.commands) {
            for (path in c.paths) {
                val pathSplit = path.split(".")
                if (pathSplit.size > 4 || pathSplit.isEmpty()) return
                for (i in pathSplit.indices) {
                    if (i == 0 && model.commandsMenus[MenuType.valueOf(pathSplit[0])] == null) break
                    m = if (i == 0) {
                        model.commandsMenus[MenuType.valueOf(pathSplit[0])]!!
                    } else {
                        m.getMenu(pathSplit[i])
                    }
                }
                m.children.add(c)
                m.children.sortBy { it.index }
            }
        }
    }

    fun addCommand(command: Command) {
        model.commands.add(command)
    }

    fun saveAll (moduleType: ModuleType) {
        saveAll(model.modules.filter { it.module.moduleType == moduleType })
    }

    private fun saveAll(modules: List<WorkbenchModuleState<*>>){
        modules.forEach{
            it.onSave()
        }
    }
}