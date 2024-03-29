
import controller.WorkbenchAction
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


internal class WorkbenchTest{

    private var sut = Workbench()
    private var controller = sut.getController()

    @BeforeEach
    fun setup(){
        sut = Workbench()
        controller = sut.getController()
    }

    @Test
    fun registerExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }, init = { _, _->}, explorerView = {})
        assertNotNull(controller.informationState.getRegisteredExplorer<String>("String"))
    }

    @Test
    fun registerEditor() {
        sut.registerEditor<String>(type = "String", initController = { _, _ -> "test"}, title = { "title" }){}
        assertEquals(1, controller.informationState.getRegisteredEditors<Any>("String").size)
    }

    @Test
    fun requestExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }, init = { _, _->}){}
        val model = "value"
        sut.requestExplorer("String", model)
        val tabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = controller.informationState.mainWindow)

        assertEquals(0, controller.informationState.getModulesFiltered(tabRowKey).size)
    }

    @Test
    fun requestExplorer_typeDoesNotExist() {
        sut.registerExplorer<String>(type = "String", title = { "title" }, init = { _, _->}){}
        val model = "value"
        assertThrows<IllegalStateException> {
            sut.requestExplorer("Other", model)
        }
    }

    @Test
    fun requestEditor() {
        sut.registerEditor(type = "String", initController = { _, _ -> "test"}, title = { "title" }){}
        sut.requestEditor("String", 0)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = controller.informationState.mainWindow)
        controller.executeAction(WorkbenchAction.InitExplorers())

        assertEquals(1, controller.informationState.getModulesFiltered(tabRowKey).size)
    }

}