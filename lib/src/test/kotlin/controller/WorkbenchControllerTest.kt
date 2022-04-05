package controller

import model.WorkbenchModel
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WorkbenchControllerTest{
    private var model = WorkbenchModel()
    private var sut = WorkbenchController(model)

    @BeforeEach
    fun setup(){
        sut = WorkbenchController(model)
    }

    @Test
    fun removeModule_NoModules() {
        val module = WorkbenchModule<String>(ModuleType.EDITOR,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ="title", model = "model", module = module, displayType = DisplayType.TAB){}
        sut.removeModuleState(moduleState)

        assertTrue { model.modules.isEmpty() }
    }

    @Test
    fun removeModule_RemoveModule() {
        val module = WorkbenchModule<String>(ModuleType.EDITOR,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ="title", model = "model", module = module, displayType = DisplayType.TAB){}
        model.modules += moduleState
        assertTrue { model.modules.contains(moduleState) }

        sut.removeModuleState(moduleState)
        assertTrue { model.modules.isEmpty() }
    }

    @Test
    fun convertToWindow() {
        val module = WorkbenchModule<String>(ModuleType.EDITOR,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ="title", model = "model", module = module, displayType = DisplayType.TAB){}

        model.modules += moduleState
        assertTrue { model.modules.contains(moduleState) }

        sut.convertToWindow(moduleState)
        assertFalse { model.modules.isEmpty() }
        assertFalse { model.modules.contains(moduleState) }
        assertEquals(1, model.modules.size)
        assertEquals(DisplayType.WINDOW, model.modules.first().displayType)
        assertEquals(module, model.modules.first().module)
    }

}