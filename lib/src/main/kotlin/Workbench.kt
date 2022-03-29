
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import model.WorkbenchModel
import model.data.WorkbenchEditor
import model.data.WorkbenchExplorer
import model.state.WorkbenchEditableState
import model.state.WorkbenchWindowState
import view.WorkbenchMainUI

class Workbench {

    private val model: WorkbenchModel = WorkbenchModel()

    /**
     * Add an explorer to the Workbench
     *
     * TODO: can this be called after run?
     * @param title: Display title of this explorer TODO: should this be a composable fun
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun registerExplorer(
        title: String,
        content: @Composable () -> Unit,
    ){
        val explorer = WorkbenchExplorer(title, content)
        //TODO: move to controller
        model.explorers.add(explorer)
        model.selectedExplorer = explorer
    }

    /**
     * Add an editor to the Workbench.
     *
     * @param T: Data which will be passed to the editor when it is requested
     * @param M: Model which the editor uses to manage and display data
     * @param title: Display title of this editor TODO: should this be a composable fun
     * @param type: the type of data this editor can be used for
     * @param initModel: the callback to be invoked when this editor is requested
     * @param onClose: the callback to be invoked when this editor is closed
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <T:Any, M:Any> registerEditor(
        title: String,
        type: WorkbenchEditorType,
        initModel: (T) -> M,
        onClose: (M) -> Unit = {},
        content: @Composable (M) -> Unit,
    ){
        model.editors.put(type , WorkbenchEditor<T, M>(title, type, initModel, onClose, content))
    }

    /**
     * Edit given data with editor of given type
     *
     * @param T: Data which is passed to the editor
     * @param M: Model which the editor uses to manage and display data
     * @param type: the type of data this editor can be used for
     * @param data: Data which is passed to the editor
     */
    fun <T, M> requestEditor(type: WorkbenchEditorType, data: T) {
        var editor = model.editors[type] as WorkbenchEditor<T, M>
        if(editor != null){
            val contentHolder = WorkbenchEditableState<T, M>(editor, data)
            model.windows.add(
                WorkbenchWindowState(title = editor.title, windowState = WindowState(), contentHolder)
            )
        }
    }

    /**
     * Run the Workbench
     *
     * Has to be called in a valid application context.
     * ```
     * application {
     *      workbench.run( onCloseRequest = ::exitApplication )
     * }
     * ```
     */
    @Composable
    fun run(onCloseRequest:  () -> Unit){
        Window(
            onCloseRequest = onCloseRequest,
            title = "Workbench", //TODO: Make configurable
        ) {
            WorkbenchMainUI(model)
        }
    }

    internal fun getModel(): WorkbenchModel {
        return model
    }
}
