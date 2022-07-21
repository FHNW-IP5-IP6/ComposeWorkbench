
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

    @BeforeEach
    fun initWorkBench() {
        sut = Workbench()
    }

    @Test
    fun registerExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){_,_->}
        assertNotNull(sut.getWorkbenchController().getRegisteredExplorer<String>("String"))
    }

    @Test
    fun registerEditor() {
        sut.registerEditor<String>(type = "String", controller = {"test"}, title = { "title" }){ _, _->}
        assertEquals(1, sut.getWorkbenchController().getRegisteredEditors<Any>("String").size)
    }

    @Test
    fun requestExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){_,_->}
        val model = "value"
        sut.requestExplorer("String", model)
        val tabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = sut.getWorkbenchController().getMainWindow())

        assertEquals(1, sut.getWorkbenchController().getModulesFiltered(tabRowKey).size)
    }

    @Test
    fun requestExplorer_typeDoesNotExist() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){_,_->}
        val model = "value"
        assertThrows<IllegalStateException> {
            sut.requestExplorer("Other", model)
        }
    }

    @Test
    fun requestEditor() {
        sut.registerEditor(type = "String", controller = {"test"}, title = { "title" }){ _, _->}
        sut.requestEditor<String>("String", 0)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.getWorkbenchController().getMainWindow())

        assertEquals(1, sut.getWorkbenchController().getModulesFiltered(tabRowKey).size)
    }

}