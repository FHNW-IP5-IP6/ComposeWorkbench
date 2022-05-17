
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application
import model.WorkbenchModel
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState
import model.state.toDisplayType
import view.WindowSpace
import view.WorkbenchMainUI


class Workbench {

    private val model: WorkbenchModel = WorkbenchModel()

    /**
     * Add an explorer to the Workbench
     *
     * TODO: can this be called after run?
     * @param type: the type of data this explorer can be used for
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <M> registerExplorer(
        type: String,
        content: @Composable (M) -> Unit,
    ){
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = content)

        model.registeredExplorers[type] = explorer
    }


    /**
     * Add an editor to the Workbench.
     *
     * @param M: Type of the Model which the editor uses to manage and display data
     * @param type: the type of data this editor can be used for
     * @param loader: callback to load model from id
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <M> registerEditor(
        type: String,
        loader: (Int) -> M,
        content: @Composable (M) -> Unit
    ){
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            loader = loader,
            content = content)
        model.registeredEditors[type] = explorer
    }

    /**
     * Explore given Model with explorer of type
     *
     * @param M: Model which the explorer uses to manage and display data
     * @param type: The type of data which the Explorer is used for
     * @param title: Display title of the requested editor
     * @param default: if Explorer with its Model is runable as Default explorer
     * @param location: The Drawer Location where the Explorer will be displayed
     * @param shown: if the Explorer is shown on the Startup of the Workbench
     */
    fun <M: Any> requestExplorer(type: String, title: String, m: M, default: Boolean = false, location: ExplorerLocation = ExplorerLocation.LEFT, shown: Boolean = true) {
        val explorer = model.registeredExplorers[type]
        if(explorer != null){
            explorer as WorkbenchModule<M>
            if (shown) {
                val state = WorkbenchModuleState(title, m, explorer, model::removeTab, toDisplayType(location))
                model.addState(state)
            }
            if (default) {
                model.registeredDefaultExplorers[title] = Pair(type, m)
            }
        }
    }

    /**
     * Edit given Model with editor of given type
     *
     * @param M: Model which the editor uses to manage and display data
     * @param id: id of the specific data which is to be edited
     * @param type: The type of data which the Editor is used for
     * @param title: Display title of the requested editor
     * @param onClose: The callback to be invoked when this editor is closed
     */
    //TODO: make title a function to display more meaningful information
    fun <M> requestEditor(type: String, title: String, id: Int, onClose: (M) -> Unit ={}, onSave: (M) -> Unit ={}) {
        val editor = model.registeredEditors[type]
        if(editor != null){
            editor as WorkbenchModule<M>
            val t = WorkbenchModuleState(title, editor.loader!!.invoke(id), editor, model::removeTab, DisplayType.TAB1, onClose, onSave)
            model.addState(t)
        }
    }

    /**
     * Run the Workbench
     */
    fun run(onExit: () -> Unit) = application {

        // init main window
        WorkbenchMainUI(model) {
            onExit.invoke()
            exitApplication()
        }

        // render seperated windows
        WindowSpace(model)
    }

    internal fun getModel(): WorkbenchModel {
        return model
    }
}
