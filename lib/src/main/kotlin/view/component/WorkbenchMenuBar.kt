package view.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import model.WorkbenchModel
import model.data.ModuleType
import model.data.SplitViewMode

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun FrameWindowScope.workbenchMenuBar(model: WorkbenchModel){
    MenuBar {
        Menu("File", mnemonic = 'F') {
            Item(
                "Save All",
                onClick = { model.saveAll(ModuleType.EDITOR) },
                shortcut = KeyShortcut(Key.S, ctrl = true, alt = true)
            )
            Item("Show Default Explorers", onClick = { model.showDefaultExplorersOverview() })
        }
        Menu("View", mnemonic = 'V') {
            Menu("Split TabSpace") {
                Item(
                    "Horizontal",
                    onClick = { model.changeSplitViewMode(SplitViewMode.HORIZONTAL) },
                    shortcut = KeyShortcut(Key.H, ctrl = true)
                )
                Item(
                    "Vertical",
                    onClick = { model.changeSplitViewMode(SplitViewMode.VERTICAL) },
                    shortcut = KeyShortcut(Key.V, ctrl = true)
                )
                Item(
                    "Unsplit",
                    onClick = { model.changeSplitViewMode(SplitViewMode.UNSPLIT) },
                    shortcut = KeyShortcut(Key.U, ctrl = true)
                )
            }
        }
    }
}