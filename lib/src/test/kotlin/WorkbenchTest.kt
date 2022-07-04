
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
        sut.registerEditor<String>(type = "String", loader = {"test"}, title = { "title" }){_,_->}
        assertEquals(1, sut.getWorkbenchController().getRegisteredEditors<Any>("String").size)
    }

    @Test
    fun openExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){_,_->}
        val model = "value"
        sut.requestExplorer<String>("String", model)
        val displayController = sut.getWorkbenchController().getDisplayController(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER)
        assertEquals(1, displayController.getModulesFiltered().size)
    }

    @Test
    fun openExplorer_typeDoesNotExist() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){_,_->}
        val model = "value"
        assertThrows<IllegalStateException> {
            sut.requestExplorer<String>("Other", model)
        }
    }

    @Test
    fun openEditor() {
        sut.registerEditor(type = "String", loader = {"test"}, title = { "title" }){_,_->}
        sut.requestEditor<String>("String", 0)
        val displayController = sut.getWorkbenchController().getDisplayController(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR)
        assertEquals(1, displayController.getModulesFiltered().size)
    }

}