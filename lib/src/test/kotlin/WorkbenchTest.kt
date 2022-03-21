
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WorkbenchTest{

    private var sut = Workbench()

    @BeforeEach
    fun setup(){
        sut = Workbench()
    }

    @Test
    fun registerExplorer() {
        sut.registerExplorer(title = "Test"){}
        assertEquals(1, sut.getModel().explorers.size)
        assertEquals("Test", sut.getModel().selectedExplorer!!.title)
    }

    @Test
    fun registerEditor() {
        addEditor("Test", getType("type"))
        assertEquals(1, sut.getModel().editors.size)
    }

    @Test
    fun openEditor() {
        val type = getType("TestType")
        addEditor("Test", type)
        sut.requestEditor<String, String>(type, "String")
        assertEquals(1, sut.getModel().windows.size)
    }

    private fun addEditor(title: String, type: WorkbenchEditorType){
        sut.registerEditor<String, String>(
            title,
            type,
            { e: String -> e },
        ){}
    }

    private fun getType(type: String): WorkbenchEditorType {
        return object :WorkbenchEditorType {
            override fun identifier(): String {
                return type
            }
        }
    }
}