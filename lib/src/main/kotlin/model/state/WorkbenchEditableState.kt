package model.state

import androidx.compose.runtime.Composable
import model.ContentHolder
import model.data.WorkbenchEditor

internal class WorkbenchEditableState<T,M>(
    private val editor: WorkbenchEditor<T, M>,
    private val data: T,
): ContentHolder {
    private val model: M = editor.initModel(data)

    fun onClose(){
        editor.onClose(model)
    }

    @Composable
    override fun content(){
        editor.content.invoke(model)
    }
}

