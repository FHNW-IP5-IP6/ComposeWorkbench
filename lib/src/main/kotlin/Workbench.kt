
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import model.WorkbenchModel
import model.state.WorkbenchEditableState
import model.state.WorkbenchEditorState
import model.state.WorkbenchExplorerState
import model.state.WorkbenchWindowState
import view.WorkbenchMainUI

class Workbench {

    private val model: WorkbenchModel = WorkbenchModel()

    fun registerExplorer(
        title: String,
        content: @Composable () -> Unit,
    ){
        val explorer = WorkbenchExplorerState(title, content)

        //TODO: move to controller
        model.explorers.add(explorer)
        model.selectedExplorer = explorer
    }

    fun <T:Any, M:Any> registerEditor(
        title: String,
        type: WorkbenchEditorType,
        initModel: (T) -> M,
        onClose: (M) -> Unit = {},
        content: @Composable (M) -> Unit,
    ){
        model.editors.put(type , WorkbenchEditorState<T, M>(title, type, initModel, onClose, content))
    }

    fun <T, M> openEditor(type: WorkbenchEditorType, data: T) {
        var editor = model.editors[type] as WorkbenchEditorState<T, M>
        if(editor != null){
            val contentHolder = WorkbenchEditableState<T, M>(editor, data)
            model.windows.add(
                WorkbenchWindowState(title = editor.title, windowState = WindowState(), contentHolder)
            )
        }
    }

    @Composable
    fun run(onCloseRequest:  () -> Unit){
        Window(
            onCloseRequest = onCloseRequest,
            title = "Workbench",
        ) {
            WorkbenchMainUI(model)
        }
    }

    internal fun getModel(): WorkbenchModel {
        return model
    }
}
