package view.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import com.example.ui.theme.NotoSansTypography
import controller.WorkbenchController
import view.themes.DarkColors
import view.themes.LightColors

@Composable
internal fun WorkbenchWindow(controller: WorkbenchController){
    key(controller.getWindows()) {
        for (state in controller.getWindows()) {
            val displayController = controller.getDisplayController(state)
            DragAndDropWindow(
                controller = controller,
                onCloseRequest =  {
                    displayController.getModulesFiltered().forEach{ it.module.onClose }
                    controller.removeWindow(state) },
                currentWindow = state
            ){
                MaterialTheme(
                    colors = if (false) DarkColors else LightColors,
                    typography = NotoSansTypography,
                ) {
                    Column {
                        DropTarget(displayController = displayController, controller = controller) {
                            WorkbenchTabRow(displayController, controller)
                        }
                        WorkbenchTabBody(displayController, controller)
                    }
                }
            }
        }
    }
}