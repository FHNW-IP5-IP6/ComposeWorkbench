
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
class WorkbenchMultiEditorModuleTest {

    private var sut = Workbench()

    class TestModel(val id: Int){
        val title = "title1"
    }

    class TestModel2(val id: Int){
        val title = "title2"
    }

    @Test
    fun twoEditorsForSameKey_SameModel() {
        //editor
        val type = "editorType"
        val id = 456
        val controller = sut.getWorkbenchController()
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel(it) }){}
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel(it) }){}
        assertEquals(2, controller.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val displayController = controller.getDisplayController(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR)

        assertEquals(1, displayController.getModulesFiltered().size)

        var module: WorkbenchModuleState<TestModel> = displayController.getSelectedModule() as WorkbenchModuleState<TestModel>
        assertEquals(id, module.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel>(type)[0], module.module)

        displayController.updateAndRefreshState(module) { module.updateModule(controller.getRegisteredEditors<TestModel>(type)[1]) }

        assertEquals(1, displayController.getModulesFiltered().size)
        module = displayController.getSelectedModule() as WorkbenchModuleState<TestModel>
        assertEquals(id, module.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel>(type)[1], module.module)
    }

    @Test
    fun twoEditorsForSameKey_DifferentModel() {
        //editor
        val controller = sut.getWorkbenchController()
        val type = "editorType"
        val id = 456
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel(it) }){}
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel2(it) }){}
        assertEquals(2, controller.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val displayController = controller.getDisplayController(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR)

        assertEquals(1, displayController.getModulesFiltered().size)
        val module1: WorkbenchModuleState<TestModel> = displayController.getSelectedModule() as WorkbenchModuleState<TestModel>
        assertEquals(id, module1.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel>(type)[0], module1.module)

        displayController.updateAndRefreshState(module1) { module1.updateModule(controller.getRegisteredEditors<TestModel2>(type)[1]) }

        assertEquals(1, displayController.getModulesFiltered().size)
        val module2: WorkbenchModuleState<TestModel2> = displayController.getSelectedModule() as WorkbenchModuleState<TestModel2>
        assertEquals(id, module2.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel2>(type)[1], module2.module)
    }
}