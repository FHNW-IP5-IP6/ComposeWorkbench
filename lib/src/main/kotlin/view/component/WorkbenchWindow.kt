package view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.WorkbenchState
import view.themes.LightColors

@Composable
internal fun WorkbenchWindow(controller: WorkbenchController, workbenchState: WorkbenchState){
    if (workbenchState == WorkbenchState.RUNNING) {
        key(controller.informationState.windows) {
            for (state in controller.informationState.windows) {
                val tabRowKey =
                    TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = state)
                if (controller.getModulesFiltered(tabRowKey).isEmpty()) {
                    controller.removeWindow(tabRowKey)
                    continue
                }
                DragAndDropWindow(
                    controller = controller,
                    onCloseRequest = {
                        controller.getModulesFiltered(tabRowKey).forEach { it.module.onClose }
                        controller.removeWindow(tabRowKey)
                    },
                    tabRowKey = tabRowKey
                ) {
                    MaterialTheme(
                        colors = LightColors,
                        typography = NotoSansTypography,
                    ) {
                        Column {
                            DropTarget(tabRowKey = tabRowKey, controller = controller) {
                                WorkbenchTabRow(tabRowKey, controller)
                            }
                            WorkbenchTabBody(tabRowKey, controller)
                        }
                    }
                }
            }
        }
    }
}