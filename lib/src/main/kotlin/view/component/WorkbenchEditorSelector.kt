package view.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import model.WorkbenchModel
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.WorkbenchModuleState

@Composable
internal fun WorkbenchEditorSelector(state: WorkbenchModuleState<*>?, model: WorkbenchModel) {
    if(state != null && ModuleType.EDITOR == state.module.moduleType){
        val editors = model.registeredEditors[state.module.modelType]!!
        if(editors.size > 1){
            Row(modifier = Modifier.fillMaxWidth()) {
                for (editor: WorkbenchModule<*> in editors){
                    IconButton(
                        onClick = {
                            model.updateModuleState(state) { it.updateModule(editor) }
                        }
                    ){
                        Icon(editor.icon!!, "")
                    }
                }
            }
        }
    }
}