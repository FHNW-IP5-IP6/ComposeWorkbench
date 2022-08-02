package view.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.MenuScope
import controller.Action
import model.data.Command
import model.data.MenuEntry
import model.data.enums.MenuType
import model.state.WorkbenchInformationState

@Composable
internal fun FrameWindowScope.workbenchMenuBar(
    informationState: WorkbenchInformationState,
    onActionRequired: (Action) -> Unit
){
    MenuBar {
        informationState.getCommandsForType(MenuType.MenuBar).forEach {
            if (it is MenuEntry) {
                Menu(it.text) {
                    MenuDispatcher(it, onActionRequired)
                }
            }
        }
    }
}

@Composable
internal fun MenuScope.MenuDispatcher(
    menuEntry: MenuEntry,
    onActionRequired: (Action) -> Unit,
){
    for (menuItem in menuEntry.children) {
        if(menuItem is MenuEntry) {
            Menu(menuItem.text) {
                MenuDispatcher(menuItem, onActionRequired)
            }
        }
        else if (menuItem is Command) {
            Item(menuItem.text, onClick = { onActionRequired.invoke(menuItem.action) }, shortcut = menuItem.shortcut )
        }
    }
}