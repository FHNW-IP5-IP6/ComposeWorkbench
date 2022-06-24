
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WorkbenchTest{

    private var sut = Workbench()

    @BeforeEach
    fun initWorkBench() {
        sut = Workbench()
    }

    @Test
    fun registerExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){}
        assertEquals(1, sut.getModel().registeredExplorers.size)
    }

    @Test
    fun registerEditor() {
        sut.registerEditor<String>(type = "String", loader = {"test"}, title = { "title" }){}
        assertEquals(1, sut.getModel().registeredEditors.size)
    }

    @Test
    fun openExplorer() {
        sut.registerExplorer<String>(type = "String", title = { "title" }){}
        val model = "value"
        sut.requestExplorer<String>("String", model)
        assertEquals(1, sut.getModel().modules.size)
    }

    @Test
    fun openEditor() {
        sut.registerEditor<String>(type = "String", loader = {"test"}, title = { "title" }){}
        val model = "value"
        sut.requestEditor<String>("String", 0)
        assertEquals(1, sut.getModel().modules.size)
    }

}