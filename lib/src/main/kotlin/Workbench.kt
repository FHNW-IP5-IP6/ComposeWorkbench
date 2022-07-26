
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
            val logMQClient = MQClient
            logMQClient.subscribe("$MQ_INTERNAL_TOPIC_PATH_EDITOR/#", ::logMQ, Executors.newSingleThreadExecutor())
        }
    }

    private val controller = WorkbenchController(appTitle)


    /**
     * Add an explorer for a given Type to the Workbench
     *
     * @param C: Type of the Controller which the explorer uses to manage and display data
     * @param type: the type of data this explorer can be used for
     * @param title: Callback to get the explorers title from a given Model
     * @param init: callback to initialize messaging and controller
     * @param explorerView: Composable function that defines the displayed content of this explorer
     */
    fun <C> registerExplorer(
        type: String,
        title: (C) -> String,
        init: (C, MQClient) -> Unit,
        explorerView: @Composable (C) -> Unit,
    ) {
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = explorerView,
            init = init,
            title = title,
        )
        controller.registerExplorer(type, explorer)
    }

    /**
     * Add an editor for a given Type to the Workbench
     *
     * @param C: Type of the Controller which the editor uses to manage and display data
     * @param type: the type of data this editor can be used for
     * @param initController: callback to load model from id
     * @param icon: Icon for this Editor. This is used in case multiple editors are registered for the same type
     * @param title: Callback to get the editors title from a given Model
     * @param onClose: The callback to be invoked when an editor of this type is closed
     * @param onSave: The callback to be invoked when an editor of this type is saved.
     *                Return true if successful otherwise false.
     *                Will publish a saved message in case of success
     * @param editorView: Composable function that defines the displayed content of this explorer
     */
    fun <C> registerEditor(
        type: String,
        title: (C) -> String,
        initController: (Int, MQClient) -> C,
        icon: ImageVector = WorkbenchDefaultIcon,
        onClose: (C, MQClient) -> ActionResult = {_,_ -> success()},
        onSave: (C, MQClient) -> ActionResult = {_,_ -> success()},
        editorView: @Composable (C) -> Unit
    ) {
        val editor = WorkbenchModule(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            icon = icon,
            loader = initController,
            content = editorView,
            title = title,
            onClose = onClose,
            onSave =  onSave
        )
        this.controller.registerEditor(type, editor)
    }

    /**
     * Explore given Controller with explorer of given type
     *
     * @param C: Controller which the explorer uses to manage and display data
     * @param type: The type of data which the Explorer is used for
     * @param listed: if Explorer with its Model is listed and accessible through menu
     * @param location: The Drawer Location where the Explorer will be displayed
     * @param shown: if the Explorer is shown on the Startup of the Workbench
     */
    fun <C : Any> requestExplorer(
        type: String,
        c: C,
        listed: Boolean = false,
        location: ExplorerLocation = ExplorerLocation.LEFT,
        shown: Boolean = true
    ) {
        val id = controller.getNextKey()
        if(shown){
            controller.requestExplorerState(id = id, modelType = type, explorerController = c, displayType =  toDisplayType(location))
        }
        if (listed) {
            controller.addDefaultExplorer(id = id, key = type, explorerModel = c)
            controller.commandController.addCommand(Command(
                text = controller.getRegisteredExplorer<C>(type).title(c),
                paths = mutableListOf(
                    "${MenuType.MenuBar.name}.View.Default Explorers",
                    "${MenuType.MenuAppBar.name}.Default Explorers"
                ),
                action = { controller.createExplorerFromDefault(id) }
            ))
        }
    }

    /**
     * Edit given Controller with editor of given type
     *
     * @param C: Controller which the editor uses to manage and display data
     * @param id: id of the specific data which is to be edited
     * @param type: The type of data which the Editor is used for
     */
    @Suppress("UNCHECKED_CAST")
    fun <C> requestEditor(
        type: String,
        id: Int
    ) {
        controller.requestEditorState<C>(modelType = type, dataId = id)
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
