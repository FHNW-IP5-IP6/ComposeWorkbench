package view.conponent

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController
import view.component.DragAndDropWindow
import view.component.DropTarget
import view.component.WorkbenchTabBody
import view.component.WorkbenchTabRow
import view.themes.DarkColors
import view.themes.LightColors

@Composable
internal fun WorkbenchWindow(controller: WorkbenchController){
    key(controller.getWindows()) {
        for (state in controller.getWindows()) {
            val displayController = controller.createWindowDisplayController(windowState = state)
            DragAndDropWindow(
                controller = controller,
                moduleReceiver = { controller.moduleToWindow(it) },
                onCloseRequest =  {
                    state.modules.forEach { it.module.onClose }
                    controller.removeWindow(state) },
                currentWindow = state
            ){
                MaterialTheme(
                    colors = if (false) DarkColors else LightColors,
                    typography = NotoSansTypography,
                ) {
                    Column {
                        DropTarget(controller = displayController) {
                            WorkbenchTabRow(displayController, controller)
                        }
                        WorkbenchTabBody(displayController, controller)
                    }
                }
            }
        }
    }
}