
import controller.WorkbenchAction
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
    private var controller = sut.getController()

    @BeforeEach
    fun setup(){
        sut = Workbench()
        controller = sut.getController()
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
        assertEquals(2, controller.informationState.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = controller.informationState.mainWindow)
        assertEquals(1, controller.informationState.getModulesFiltered(tabRowKey).size)

        var moduleState: WorkbenchModuleState<TestModel> = controller.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState.controller.id)
        assertEquals(controller.informationState.getRegisteredEditors<TestModel>(type)[0], moduleState.module)

        controller.triggerAction(WorkbenchAction.UpdateModuleState(moduleState, controller.informationState.getRegisteredEditors<TestModel>(type)[1]))

        assertEquals(1, controller.informationState.getModulesFiltered(tabRowKey).size)
        moduleState = controller.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState.controller.id)
        assertEquals(controller.informationState.getRegisteredEditors<TestModel>(type)[1], moduleState.module)
    }

    @Test
    fun twoEditorsForSameKey_DifferentModel() {
        //editor
        val type = "editorType"
        val id = 456
        sut.registerEditor(type = type, title = {it.title}, initController = { i, _ ->  TestModel(i) }){ }
        sut.registerEditor(type = type, title = {it.title}, initController = { i, _ ->  TestModel2(i) }){ }
        assertEquals(2, controller.informationState.getRegisteredEditors<TestModel>(type).size)

        sut.requestEditor<TestModel>(type = type, id = id)
        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = controller.informationState.mainWindow)

        assertEquals(1, controller.informationState.getModulesFiltered(tabRowKey).size)
        val moduleState1: WorkbenchModuleState<TestModel> = controller.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel>
        assertEquals(id, moduleState1.controller.id)
        assertEquals(controller.informationState.getRegisteredEditors<TestModel>(type)[0], moduleState1.module)

        controller.triggerAction(WorkbenchAction.UpdateModuleState(moduleState1, controller.informationState.getRegisteredEditors<TestModel2>(type)[1]))

        assertEquals(1, controller.informationState.getModulesFiltered(tabRowKey).size)
        val module2: WorkbenchModuleState<TestModel2> = controller.informationState.getSelectedModule(tabRowKey) as WorkbenchModuleState<TestModel2>
        assertEquals(id, module2.controller.id)
        assertEquals(controller.informationState.getRegisteredEditors<TestModel2>(type)[1], module2.module)
    }
}