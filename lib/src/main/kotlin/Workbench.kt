
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import com.hivemq.embedded.EmbeddedHiveMQ
import com.hivemq.embedded.EmbeddedHiveMQBuilder
import model.WorkbenchModel
import model.data.*
import model.data.ModuleType
import model.data.WorkbenchModule
import model.data.toDisplayType
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import view.WorkbenchUI
import view.conponent.WorkbenchWindow
import java.util.concurrent.ExecutorService
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
     * TODO: can this be called after run?
     * @param M: Type of the Model which the explorer uses to manage and display data
     * @param type: the type of data this explorer can be used for
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <M> registerExplorer(
        type: String,
        content: @Composable (M) -> Unit,
    ) {
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = content
        )

        model.registeredExplorers[type] = explorer
    }


    /**
     * Add an editor for a given Type to the Workbench
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
    ) {
        val editor = WorkbenchModule(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            loader = loader,
            content = content
        )
        model.registeredEditors[type] = editor
    }

    /**
     * Explore given Model with explorer of given type
     *
     * @param M: Model which the explorer uses to manage and display data
     * @param type: The type of data which the Explorer is used for
     * @param title: Display title of the requested editor
     * @param default: if Explorer with its Model is runnable as Default explorer
     * @param location: The Drawer Location where the Explorer will be displayed
     * @param shown: if the Explorer is shown on the Startup of the Workbench
     */
    @Suppress("UNCHECKED_CAST")
    fun <M : Any> requestExplorer(
        type: String,
        title: (M) -> String,
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
                val state = WorkbenchModuleState(id, title, m, explorer, model::removeTab, toDisplayType(location))
                model.addState(state)
            }
            if (default) {
                model.registeredDefaultExplorers[id] = WorkbenchDefaultState(type, m, title)
                model.commands.add(Command(
                    text = model.registeredDefaultExplorers[id]!!.getTitle(),
                    path = "$MENU_IDENTIFIER_MENU_BAR.View.Default Explorers",
                    action = { model.createExplorerFromDefault(id) }
                ))
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
    @Suppress("UNCHECKED_CAST")
    fun <M> requestEditor(
        type: String,
        title: (M) -> String,
        id: Int,
        onClose: (M) -> Unit = {},
        onSave: (M) -> Unit = {}
    ) {
        val editor = model.registeredEditors[type]
        if (editor != null) {
            editor as WorkbenchModule<M>
            val t = WorkbenchModuleState(
                id = model.getNextKey(),
                title = title,
                model = editor.loader!!.invoke(id),
                module = editor,
                close = model::removeTab,
                displayType = model.currentTabSpace,
                onClose = onClose,
                onSave = onSave
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

    internal fun getModel(): WorkbenchModel {
        return model
    }

    private fun stopMQBroker() {
        try {
            hiveMQ?.stop()?.join();
        } catch (ex: Exception ) {
            ex.printStackTrace();
        }
    }

    private fun logMQ(msg: String) {
        println("Log-MQ: $msg")
    }


}
