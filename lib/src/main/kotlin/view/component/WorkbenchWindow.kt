package view.conponent

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchWindowController
import model.WorkbenchModel
import view.component.DragAndDropWindow
import view.component.DropTarget
import view.component.WorkbenchTabBody
import view.component.WorkbenchTabRow
import view.themes.DarkColors
import view.themes.LightColors

@Composable
internal fun WorkbenchWindow(model: WorkbenchModel){
    key(model.windows) {
        for (state in model.windows) {
            val controller = WorkbenchWindowController(model = model, windowState = state)
            DragAndDropWindow(
                model = model,
                moduleReceiver = { controller.convertToWindow(it) },
                onCloseRequest =  {
                    state.modules.forEach { it.module.onClose }
                    model.windows.remove(state) },
                currentWindow = state
            ){
                MaterialTheme(
                    colors = if (false) DarkColors else LightColors,
                    typography = NotoSansTypography,
                ) {
                    Column {
                        DropTarget(controller = controller) {
                            WorkbenchTabRow(controller)
                        }
                        WorkbenchTabBody(controller)
                    }
                }
            }
        }
    }
}