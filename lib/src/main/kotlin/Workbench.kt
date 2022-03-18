
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import model.WorkbenchModel
import model.state.WorkbenchEditorState
import model.state.WorkbenchExplorerState
import model.state.WorkbenchWindowState
import view.WorkbenchMainUI

object Workbench { //TODO: Should this be a class

    fun registerExplorer(
        title: String,
        content: @Composable () -> Unit,
    ){
        val explorer = WorkbenchExplorerState(title, content)
        WorkbenchModel.explorers.add(explorer)
        WorkbenchModel.selectedExplorer = explorer
    }

    fun <T:Any, M:Any> registerEditor(
        title: String,
        type: WorkbenchEditorType,
        initModel: (T) -> M,
        content: @Composable (M) -> Unit,
    ){
        WorkbenchModel.editors.put(type , WorkbenchEditorState<T, M>(title, type, initModel ,content))
    }

    fun <T, M> openEditor(type: WorkbenchEditorType, data: T) {
        var editor = WorkbenchModel.editors[type] as WorkbenchEditorState<T, M>
        if(editor != null){
            WorkbenchModel.windows.add(
                WorkbenchWindowState(
                    editor.title,
                ){
                    editor.content.invoke(editor.initModel(data))
                }
            )
        }
    }

    @Composable
    fun run(onCloseRequest:  () -> Unit){
        Window(
            onCloseRequest = onCloseRequest,
            title = "Workbench",
        ) {
            WorkbenchMainUI(WorkbenchModel)
        }
    }
}
