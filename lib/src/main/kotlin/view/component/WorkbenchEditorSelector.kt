package view.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import controller.WorkbenchController
import controller.WorkbenchDisplayController
import model.data.enums.ModuleType
import model.data.WorkbenchModule

@Composable
internal fun WorkbenchEditorSelector(displayController: WorkbenchDisplayController, controller: WorkbenchController) {
    val state = displayController.getSelectedModule()
    if(state != null && ModuleType.EDITOR == state.module.moduleType){
        val editors = controller.getRegisteredEditors<Any>(state.module.modelType)
        if(editors.size > 1){
            Row(modifier = Modifier.fillMaxWidth()) {
                for (editor: WorkbenchModule<*> in editors){
                    IconButton(
                        onClick = {
                            displayController.updateAndRefreshState(state) {
                                it.updateModule(editor)
                            }
                        }
                    ){
                        Icon(editor.icon, "")
                    }
                }
            }
        }
    }
}