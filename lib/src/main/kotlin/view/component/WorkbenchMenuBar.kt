    package view.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import controller.WorkbenchCommandController
import model.data.Command
import model.data.MenuEntry
import model.data.enums.MenuType

@Composable
internal fun FrameWindowScope.workbenchMenuBar(commandController: WorkbenchCommandController){
    MenuBar {
        commandController.getCommandsForType(MenuType.MenuBar).forEach {
            if (it is MenuEntry) {
                Menu(it.text) {
                    MenuDispatcher(it)
                }
            }
        }
    }
}

@Composable
internal fun MenuScope.MenuDispatcher(menuEntry: MenuEntry){
    for (menuItem in menuEntry.children) {
        if(menuItem is MenuEntry) {
            Menu(menuItem.text) {
                MenuDispatcher(menuItem)
            }
        }
        else if (menuItem is Command) {
            Item(menuItem.text, onClick = menuItem.action, shortcut = menuItem.shortcut )
        }
    }
}