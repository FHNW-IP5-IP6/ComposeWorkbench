
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.application
import com.hivemq.embedded.EmbeddedHiveMQ
import com.hivemq.embedded.EmbeddedHiveMQBuilder
import controller.WorkbenchController
import model.data.Command
import model.data.MQClient
import model.data.WorkbenchModule
import model.data.enums.MenuType
import model.data.enums.ModuleType
import model.data.enums.toDisplayType
import util.WorkbenchDefaultIcon
import view.WorkbenchUI
import view.component.WorkbenchWindow
import java.util.concurrent.Executors


class Workbench(appTitle: String = "", enableMQ: Boolean = false) {

    // HiveMQ infrastructure
    // IMPORTANT!!! has to be first instance to initiate, otherwise early clients won't connect to broker
    // TODO: reconnect of clients after not reaching client?
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
            val logMQClient = MQClient("workbench-log", 0)
            logMQClient.subscribe("$MQ_INTERNAL_TOPIC_PATH_EDITOR/#", ::logMQ, Executors.newSingleThreadExecutor())
        }
    }

    private val controller = WorkbenchController(appTitle)


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
        content: @Composable (M, MQClient) -> Unit,
    ) {
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = content,
            title = title,
            onClose = {_,_ ->},
            onSave = {_,_ -> true}
        )
        controller.registerExplorer(type, explorer)
    }

    /**
     * Add an editor for a given Type to the Workbench
     *
     * @param C: Type of the Model which the editor uses to manage and display data
     * @param type: the type of data this editor can be used for
     * @param controller: callback to load model from id
     * @param icon: Icon for this Editor. This is used in case multiple editors are registered for the same type
     * @param title: Callback to get the editors title from a given Model
     * @param onClose: The callback to be invoked when an editor of this type is closed
     * @param onSave: The callback to be invoked when an editor of this type is saved
     * @param content: Composable function that defines the displayed content of this explorer
     */
    fun <C> registerEditor(
        type: String,
        title: (C) -> String,
        controller: (Int, MQClient) -> C,
        icon: ImageVector = WorkbenchDefaultIcon,
        onClose: (C, MQClient) -> Unit = {_,_ ->},
        onSave: (C, MQClient) -> Boolean = {_,_ -> true},
        content: @Composable (C, MQClient) -> Unit
    ) {
        val editor = WorkbenchModule(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            icon = icon,
            loader = controller,
            content = content,
            title = title,
            onClose = onClose,
            onSave =  onSave
        )
        this.controller.registerEditor(type, editor)
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
    fun <M : Any> requestExplorer(
        type: String,
        m: M,
        default: Boolean = false,
        location: ExplorerLocation = ExplorerLocation.LEFT,
        shown: Boolean = true
    ) {
        val id = controller.getNextKey()
        if(shown){
            controller.requestExplorerState(id = id, moduleType = type, explorerModel = m, displayType =  toDisplayType(location))
        }
        if (default) {
            controller.addDefaultExplorer(id = id, key = type, explorerModel = m)
            controller.commandController.addCommand(Command(
                text = controller.getRegisteredExplorer<M>(type).title(m),
                paths = mutableListOf(
                    "${MenuType.MenuBar.name}.View.Default Explorers",
                    "${MenuType.MenuAppBar.name}.Default Explorers"
                ),
                action = { controller.createExplorerFromDefault(id) }
            ))
        }
    }

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
        controller.requestEditorState<M>(moduleType = type, dataId = id)
    }

    /**
     * Run the Workbench
     */
    fun run(onExit: () -> Unit) = application {
        // pre processing
        controller.commandController.dispatchCommands()

        // init main window
        WorkbenchUI(
            controller = controller
        ) {
            onExit.invoke()
            stopMQBroker()
            exitApplication()
        }

        // init separated windows
        WorkbenchWindow(controller = controller)
    }

    private fun stopMQBroker() {
        try {
            hiveMQ?.stop()?.join()
        } catch (ex: Exception ) {
            ex.printStackTrace()
        }
    }

    private fun logMQ(topic: String, msg: String) {
        println("Log-MQ:$topic: $msg")
    }

    //used for testing
    internal fun getWorkbenchController(): WorkbenchController {
        return controller
    }
}
