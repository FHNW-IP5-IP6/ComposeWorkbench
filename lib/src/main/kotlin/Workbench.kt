
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.application
import com.hivemq.embedded.EmbeddedHiveMQ
import com.hivemq.embedded.EmbeddedHiveMQBuilder
import model.WorkbenchModel
import model.data.*
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import util.WorkbenchDefaultIcon
import view.WorkbenchUI
import view.conponent.WorkbenchWindow
import java.util.concurrent.Executors


class Workbench(appTitle: String = "", enableMQ: Boolean = false) {

    private val model: WorkbenchModel = WorkbenchModel(appTitle)

    // HiveMQ infrastructure

    private var hiveMQ: EmbeddedHiveMQ? = null
    init {
        if (enableMQ) {
            try {
                val embeddedHiveMQBuilder: EmbeddedHiveMQBuilder = EmbeddedHiveMQ.builder()
                hiveMQ = embeddedHiveMQBuilder.build()
                hiveMQ!!.start().join()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            // subscribe for available topics to log
            val logMQClient = MQClient("ComposeWorkbenchLog")
            logMQClient.subscribe(MQ_INTERNAL_TOPIC_PATH_EDITOR, ::logMQ, Executors.newSingleThreadExecutor())
        }
    }

    /**
     * Add an explorer for a given Type to the Workbench
     *
     *
     * @param M: Type of the Model which the explorer uses to manage and display data
     * @param type: the type of data this explorer can be used for
     * @param title: Callback to get the explorers title from a given Model
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <M> registerExplorer(
        type: String,
        title: (M) -> String,
        content: @Composable (M) -> Unit,
    ) {
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = content,
            title = title,
            onClose = {},
            onSave = {}
        )

        model.registeredExplorers[type] = explorer
    }

    /**
     * Add an editor for a given Type to the Workbench
     *
     * @param M: Type of the Model which the editor uses to manage and display data
     * @param type: the type of data this editor can be used for
     * @param loader: callback to load model from id
     * @param icon: Icon for this Editor. This is used in case multiple editors are registered for the same type
     * @param title: Callback to get the editors title from a given Model
     * @param onClose: The callback to be invoked when an editor of this type is closed
     * @param onSave: The callback to be invoked when an editor of this type is saved
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <M> registerEditor(
        type: String,
        title: (M) -> String,
        loader: (Int) -> M,
        icon: ImageVector = WorkbenchDefaultIcon,
        onClose: (M) -> Unit = {},
        onSave: (M) -> Unit = {},
        content: @Composable (M) -> Unit
    ) {
        val editor = WorkbenchModule(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            icon = icon,
            loader = loader,
            content = content,
            title = title,
            onClose = onClose,
            onSave =  onSave
        )
        model.registerEditor(type, editor)
    }

    /**
     * Explore given Model with explorer of given type
     *
     * @param M: Model which the explorer uses to manage and display data
     * @param type: The type of data which the Explorer is used for
     * @param default: if Explorer with its Model is runnable as Default explorer
     * @param location: The Drawer Location where the Explorer will be displayed
     * @param shown: if the Explorer is shown on the Startup of the Workbench
     */
    @Suppress("UNCHECKED_CAST")
    fun <M : Any> requestExplorer(
        type: String,
        m: M,
        default: Boolean = false,
        location: ExplorerLocation = ExplorerLocation.LEFT,
        shown: Boolean = true
    ) {
        val explorer = model.registeredExplorers[type]
        if (explorer != null) {
            explorer as WorkbenchModule<M>
            val id = model.getNextKey()
            if (shown) {
                val state = WorkbenchModuleState(id = id, model =  m, module = explorer, close =  model::removeTab, displayType =  toDisplayType(location))
                model.addState(state)
            }
            if (default) {
                model.registeredDefaultExplorers[id] = WorkbenchDefaultState(type, m, explorer.title)
                model.addCommand(Command(
                    text = model.registeredDefaultExplorers[id]!!.getTitle(),
                    paths = mutableListOf(
                        "${MenuType.MenuBar.name}.View.Default Explorers",
                        "${MenuType.MenuAppBar.name}"
                    ),
                    action = { model.createExplorerFromDefault(id) }
                ))
            }
        }
    }

    //TODO: Remove type info here move everything to the module -> title and on close on save
    /**
     * Edit given Model with editor of given type
     *
     * @param M: Model which the editor uses to manage and display data
     * @param id: id of the specific data which is to be edited
     * @param type: The type of data which the Editor is used for
     */
    @Suppress("UNCHECKED_CAST")
    fun <M> requestEditor(
        type: String,
        id: Int
    ) {
        val editors = model.registeredEditors[type]
        if (editors != null && editors.isNotEmpty()) {
            val editor = editors[0] as WorkbenchModule<M>
            val t = WorkbenchModuleState(
                id = model.getNextKey(),
                dataId = id,
                model = editor.loader!!.invoke(id),
                module = editor,
                close = model::removeTab,
                displayType = model.currentTabSpace,
            )
            model.addState(t)
        }
    }

    /**
     * Run the Workbench
     */
    fun run(onExit: () -> Unit) = application {
        // pre processing
        model.dispatchCommands()

        // init main window
        WorkbenchUI(model) {
            onExit.invoke()
            stopMQBroker()
            exitApplication()
        }

        // init separated windows
        WorkbenchWindow(model)
    }

    private fun stopMQBroker() {
        try {
            hiveMQ?.stop()?.join()
        } catch (ex: Exception ) {
            ex.printStackTrace()
        }
    }

    private fun logMQ(msg: String) {
        println("Log-MQ: $msg")
    }

    //used for testing
    internal fun getModel(): WorkbenchModel {
        return model
    }
}
