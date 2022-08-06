
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.application
import com.example.ui.theme.NotoSansTypography
import com.hivemq.embedded.EmbeddedHiveMQ
import com.hivemq.embedded.EmbeddedHiveMQBuilder
import controller.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import model.data.Command
import model.data.MQClientImpl
import model.data.MqClient
import model.data.WorkbenchModule
import model.data.enums.MenuType
import model.data.enums.ModuleType
import model.data.enums.WorkbenchState
import model.data.enums.toDisplayType
import model.state.WorkbenchDefaultState
import model.state.WorkbenchModuleState
import util.WorkbenchDefaultIcon
import view.WorkbenchUI
import view.component.WorkbenchDragAnimation
import view.component.WorkbenchWindow
import view.themes.LightColors
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class Workbench(private val appTitle: String = "", private val enableMQ: Boolean = false) {

    private val actionChannel = Channel<Action> (capacity = Channel.UNLIMITED)

    private var workbenchState by mutableStateOf(WorkbenchState.STARTING)
    private val controller = WorkbenchController()

    private var hiveMQ: EmbeddedHiveMQ? = null
    private var mqController: WorkbenchMQDispatcher? = null

    private val initJob: Job

    private fun triggerAction(action: Action) = runBlocking {
        launch {
            actionChannel.send(action)
        }
        if (action is WorkbenchActionSync) {
            action.response.await()
        }
    }

    private fun startConsumingActions() {
        CoroutineScope(Dispatchers.Default).launch {
            actionChannel.consumeEach { action ->
                controller.triggerAction(action)
                if (action is WorkbenchActionSync) {
                    action.response.complete(0)
                }
            }
        }
    }

    init {
        startConsumingActions()
        triggerAction(WorkbenchAction.SetAppTitle(appTitle))
        initJob = initMQInfrastructure()
    }

    private fun initMQInfrastructure() = CoroutineScope(Dispatchers.Default).launch {
        if (enableMQ) {
            try {
                val embeddedHiveMQBuilder: EmbeddedHiveMQBuilder = EmbeddedHiveMQ.builder()
                hiveMQ = embeddedHiveMQBuilder.build()
                hiveMQ!!.start().join()

                // subscribe for available topics to log
                val logMQClient = MQClientImpl
                logMQClient.subscribe(
                    "$MQ_INTERNAL_TOPIC_PATH_EDITOR/#",
                    ::logMQ,
                    Executors.newSingleThreadExecutor()
                )
                mqController = WorkbenchMQDispatcher {
                    triggerAction(it)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

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
        init: (C, MqClient) -> Unit,
        explorerView: @Composable (C) -> Unit,
    ) {
        val explorer = WorkbenchModule(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = explorerView,
            init = init,
            title = title,
        )
        triggerAction(WorkbenchActionSync.RegisterExplorer(type, explorer))
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
        initController: (Int, MqClient) -> C,
        icon: ImageVector = WorkbenchDefaultIcon,
        onClose: (C, MqClient) -> ActionResult = { _, _ -> success() },
        onSave: (C, MqClient) -> ActionResult = { _, _ -> success() },
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
            onSave = onSave
        )
        triggerAction(WorkbenchActionSync.RegisterEditor(type, editor))
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
        val explorer = controller.informationState.getRegisteredExplorer<C>(type)
        val defaultExplorer = WorkbenchDefaultState(explorer.modelType, c, explorer.title, location, shown, listed)
        triggerAction(WorkbenchActionSync.AddDefaultExplorer(id, defaultExplorer))
        if (listed) {
            controller.triggerAction(
                WorkbenchActionSync.AddCommand(
                    Command(
                        text = controller.informationState.getRegisteredExplorer<C>(type).title(c),
                        paths = mutableListOf(
                            "${MenuType.MenuBar.name}.View.Default Explorers",
                            "${MenuType.MenuAppBar.name}.Default Explorers"
                        ),
                        action = WorkbenchAction.CreateExplorerFromDefault(id)
                    )
                )
            )
        }
        if (WorkbenchState.RUNNING == workbenchState) {
            val moduleState = WorkbenchModuleState<C>(
                id = id,
                controller = c,
                module = explorer,
                window = controller.informationState.mainWindow,
                displayType = toDisplayType(location),
            )
            triggerAction(WorkbenchActionSync.RequestExplorerState(moduleState = moduleState))
        }
    }

    /**
     * Edit given Controller with editor of given type
     *
     * @param id: id of the specific data which is to be edited
     * @param type: The type of data which the Editor is used for
     */
    @Suppress("UNCHECKED_CAST")
    fun requestEditor(
        type: String,
        id: Int
    ) {
        triggerAction(WorkbenchActionSync.RequestEditorState(type, id))
    }

    /**
     * Run the Workbench
     */
    @OptIn(InternalCoroutinesApi::class)
    fun run(onExit: () -> ActionResult) {
        // if already initialized invokeOnCompletion is called immediately
        initJob.invokeOnCompletion(onCancelling = false) {
            if(workbenchState == WorkbenchState.TERMINATING) return@invokeOnCompletion
            workbenchState = WorkbenchState.RUNNING
            controller.triggerAction(WorkbenchAction.InitExplorers())
            controller.dispatchCommands()
        }
        initUI(onExit)
    }

    private fun initUI (onExit: () -> ActionResult) = application {
        // init main window
        val informationState = controller.informationState
        MaterialTheme(
            colors = LightColors,
            typography = NotoSansTypography,
        ) {
            WorkbenchUI(
                informationState = informationState,
                onActionRequired = { triggerAction(it) },
                workbenchState = workbenchState,
            ) {
                if (onExit.invoke().successful) {
                    workbenchState = WorkbenchState.TERMINATING
                    initJob.cancel()
                    terminatingWorkbenchAsync(this)
                }
            }

            // init separated windows
            WorkbenchWindow(
                informationState = informationState,
                onActionRequired = { triggerAction(it) },
                workbenchState = workbenchState
            )

            WorkbenchDragAnimation(dragState = controller.dragState)
        }
    }

    private fun terminatingWorkbenchAsync(applicationScope: ApplicationScope) = CoroutineScope(Dispatchers.Default).launch {
        try {
            hiveMQ?.stop()?.join()
        } catch (ex: Exception ) {
            ex.printStackTrace()
        }finally {
            actionChannel.close()
            applicationScope.exitApplication()
        }
    }

    private fun logMQ(topic: String, msg: String) {
        println("Log-MQ:$topic: $msg")
    }

    //use for testing only
    internal fun getController(): WorkbenchController{
        return controller
    }
}
