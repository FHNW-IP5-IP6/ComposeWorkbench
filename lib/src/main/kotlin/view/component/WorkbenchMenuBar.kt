package view.component

import COMMAND_IDENTIFIER_MENU_BAR
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import model.WorkbenchModel
import model.data.Command
import model.data.MenuEntry

@Composable
internal fun FrameWindowScope.workbenchMenuBar(model: WorkbenchModel){
    MenuBar {
        model.commandsMenus[COMMAND_IDENTIFIER_MENU_BAR]!!.children.forEach {
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