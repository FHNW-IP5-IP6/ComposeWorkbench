
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("UNCHECKED_CAST")
class WorkbenchEditorTest {

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
        val model = sut.getModel()
        val type = "editorType"
        val id = 456
        sut.registerEditor<TestModel>(type = type, title = {it.title}, loader = { TestModel(it) }){}
        sut.registerEditor<TestModel>(type = type, title = {it.title}, loader = { TestModel(it) }){}

        assertEquals(2, model.registeredEditors[type]!!.size)

        sut.requestEditor<TestModel>(type = type, id = id)

        assertEquals(1, model.modules.size)
        var module: WorkbenchModuleState<TestModel> = model.modules[0] as WorkbenchModuleState<TestModel>
        assertEquals(id, module.model.id)
        assertEquals(model.registeredEditors[type]!![0], module.module)

        model.updateModuleState(module) { module.updateModule(model.registeredEditors[type]!![1]) }

        assertEquals(1, model.modules.size)
        module = model.modules[0] as WorkbenchModuleState<TestModel>
        assertEquals(id, module.model.id)
        assertEquals(model.registeredEditors[type]!![1], module.module)
    }

    @Test
    fun twoEditorsForSameKey_DifferentModel() {
        //editor
        val model = sut.getModel()
        val type = "editorType"
        val id = 456
        sut.registerEditor<TestModel>(type = type, title = {it.title}, loader = { TestModel(it) }){}
        sut.registerEditor<TestModel2>(type = type, title = {it.title}, loader = { TestModel2(it) }){}

        assertEquals(2, model.registeredEditors[type]!!.size)

        sut.requestEditor<TestModel>(type = type, id = id)

        assertEquals(1, model.modules.size)
        val module1: WorkbenchModuleState<TestModel> = model.modules[0] as WorkbenchModuleState<TestModel>
        assertEquals(id, module1.model.id)
        assertEquals(model.registeredEditors[type]!![0], module1.module)

        model.updateModuleState(module1) { module1.updateModule(model.registeredEditors[type]!![1]) }

        assertEquals(1, model.modules.size)
        val module2: WorkbenchModuleState<TestModel2> = model.modules[0] as WorkbenchModuleState<TestModel2>
        assertEquals(id, module2.model.id)
        assertEquals(model.registeredEditors[type]!![1], module2.module)
    }
}