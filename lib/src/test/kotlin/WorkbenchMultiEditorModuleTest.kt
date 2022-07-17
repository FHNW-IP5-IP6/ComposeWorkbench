
import model.data.TabRowKey
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
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel(it) }){_,_->}
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel(it) }){_,_->}
        assertEquals(2, controller.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.getWorkbenchController().getMainWindow())
        assertEquals(1, controller.getModulesFiltered(tabRowKey).size)

        var moduleState: WorkbenchModuleState<TestModel> = controller.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel>(type)[0], moduleState.module)

        controller.updateModule(moduleState, controller.getRegisteredEditors<TestModel>(type)[1])

        assertEquals(1, controller.getModulesFiltered(tabRowKey).size)
        moduleState = controller.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel>(type)[1], moduleState.module)
    }

    @Test
    fun twoEditorsForSameKey_DifferentModel() {
        //editor
        val controller = sut.getWorkbenchController()
        val type = "editorType"
        val id = 456
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel(it) }){ _, _->}
        sut.registerEditor(type = type, title = {it.title}, loader = { TestModel2(it) }){ _, _->}
        assertEquals(2, controller.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.getWorkbenchController().getMainWindow())

        assertEquals(1, controller.getModulesFiltered(tabRowKey).size)
        val moduleState1: WorkbenchModuleState<TestModel> = controller.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState1.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel>(type)[0], moduleState1.module)

        controller.updateModule(moduleState1, controller.getRegisteredEditors<TestModel2>(type)[1])

        assertEquals(1, controller.getModulesFiltered(tabRowKey).size)
        val module2: WorkbenchModuleState<TestModel2> = controller.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel2>
        assertEquals(id, module2.model.id)
        assertEquals(controller.getRegisteredEditors<TestModel2>(type)[1], module2.module)
    }
}