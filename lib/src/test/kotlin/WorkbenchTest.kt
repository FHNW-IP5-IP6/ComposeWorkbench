
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
        sut.registerExplorer<String>(type = "String"){}
        assertEquals(1, sut.getModel().registeredExplorers.size)
    }

    @Test
    fun registerEditor() {
        sut.registerEditor<String>(type = "String", loader = {"test"}){}
        assertEquals(1, sut.getModel().registeredEditors.size)
    }

    @Test
    fun openExplorer() {
        sut.registerExplorer<String>(type = "String"){}
        val model = "value"
        sut.requestExplorer<String>("String", {"String Explorer"}, model)
        assertEquals(2, sut.getModel().modules.size)
    }

    @Test
    fun openEditor() {
        sut.registerEditor<String>(type = "String", loader = {"test"}){}
        val model = "value"
        sut.requestEditor<String>("String", {"String Editor"}, 1)
        assertEquals(2, sut.getModel().modules.size)
    }

}