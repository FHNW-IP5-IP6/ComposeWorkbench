
import controller.WorkbenchController
import model.data.TabRowKey
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
class WorkbenchMultiEditorModuleTest {

    private var sut = Workbench()

    @BeforeEach
    fun setup(){
        WorkbenchController.resetInformationState()
    }

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
        sut.registerEditor(type = type, title = {it.title}, initController = { i, _ -> TestModel(i) }){ }
        sut.registerEditor(type = type, title = {it.title}, initController = { i, _ ->TestModel(i) }){ }
        assertEquals(2, WorkbenchController.informationState.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = WorkbenchController.informationState.mainWindow)
        assertEquals(1, WorkbenchController.informationState.getModulesFiltered(tabRowKey).size)

        var moduleState: WorkbenchModuleState<TestModel> = WorkbenchController.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState.controller.id)
        assertEquals(WorkbenchController.informationState.getRegisteredEditors<TestModel>(type)[0], moduleState.module)

        WorkbenchController.updateModule(moduleState, WorkbenchController.informationState.getRegisteredEditors<TestModel>(type)[1])

        assertEquals(1, WorkbenchController.informationState.getModulesFiltered(tabRowKey).size)
        moduleState = WorkbenchController.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState.controller.id)
        assertEquals(WorkbenchController.informationState.getRegisteredEditors<TestModel>(type)[1], moduleState.module)
    }

    @Test
    fun twoEditorsForSameKey_DifferentModel() {
        //editor
        val type = "editorType"
        val id = 456
        sut.registerEditor(type = type, title = {it.title}, initController = { i, _ ->  TestModel(i) }){ }
        sut.registerEditor(type = type, title = {it.title}, initController = { i, _ ->  TestModel2(i) }){ }
        assertEquals(2, WorkbenchController.informationState.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = WorkbenchController.informationState.mainWindow)

        assertEquals(1, WorkbenchController.informationState.getModulesFiltered(tabRowKey).size)
        val moduleState1: WorkbenchModuleState<TestModel> = WorkbenchController.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState1.controller.id)
        assertEquals(WorkbenchController.informationState.getRegisteredEditors<TestModel>(type)[0], moduleState1.module)

        WorkbenchController.updateModule(moduleState1, WorkbenchController.informationState.getRegisteredEditors<TestModel2>(type)[1])

        assertEquals(1, WorkbenchController.informationState.getModulesFiltered(tabRowKey).size)
        val module2: WorkbenchModuleState<TestModel2> = WorkbenchController.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel2>
        assertEquals(id, module2.controller.id)
        assertEquals(WorkbenchController.informationState.getRegisteredEditors<TestModel2>(type)[1], module2.module)
    }
}