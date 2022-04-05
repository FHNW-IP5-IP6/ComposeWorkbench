
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application
import model.WorkbenchModel
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState
import view.WindowSpace
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
    fun <M> registerExplorer(
        type: String,
        content: @Composable (M) -> Unit,
    ){
        val explorer = WorkbenchModule<M>(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = content)

        model.registeredExplorers.put(type, explorer);
    }

    /**
     * Add an editor to the Workbench.
     *
     * @param M: Type of the Model which the editor uses to manage and display data
     * @param type: the type of data this editor can be used for
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <M> registerEditor(
        type: String,
        content: @Composable (M) -> Unit
    ){
        val explorer = WorkbenchModule<M>(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            content = content)
        model.registeredEditors.put(type, explorer);
    }

    /**
     * Explore given Model with explorer of type
     *
     * @param M: Model which the explorer uses to manage and display data
     * @param type: The type of data which the Explorer is used for
     * @param title: Display title of the requested editor
     */
    fun <M> requestExplorer(type: String, title: String, m: M) {
        var explorer = model.registeredExplorers.get(type)
        if(explorer != null){
            explorer as WorkbenchModule<M>
            val state = WorkbenchModuleState(title, m, explorer, model::removeTab, DisplayType.RIGHT)
            model.modules += state
            model.selectedExplorer = state
        }
    }

    /**
     * Edit given Model with editor of given type
     *
     * @param M: Model which the editor uses to manage and display data
     * @param type: The type of data which the Editor is used for
     * @param title: Display title of the requested editor
     * @param onClose: The callback to be invoked when this editor is closed
     */
    fun <M> requestEditor(type: String, title: String, m: M, onClose: (M) -> Unit ={}) {
        var editor = model.registeredEditors.get(type)
        if(editor != null){
            editor as WorkbenchModule<M>
            val t = WorkbenchModuleState<M>(title, m, editor, model::removeTab, DisplayType.WINDOW, onClose)
            model.modules += t
            model.selectedTab = t
        }
    }

    /**
     * Run the Workbench
     */
    fun run() = application {
        // init mainwindow
        WorkbenchMainUI(model, ::exitApplication)

        // render seperated windows
        WindowSpace(model)
    }

    internal fun getModel(): WorkbenchModel {
        return model
    }
}
