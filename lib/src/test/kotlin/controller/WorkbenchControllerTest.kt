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
    private val displayType = DisplayType.TAB
    private val moduleType = ModuleType.EDITOR
    private var model = WorkbenchModel()
    private var sut = WorkbenchModuleController(model, displayType, moduleType)

    @BeforeEach
    fun setup(){
        sut = WorkbenchModuleController(model, displayType, moduleType)
    }

    @Test
    fun removeModule_NoModules() {
        val module = WorkbenchModule<String>(moduleType,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ="title", model = "model", module = module, displayType = displayType){}
        sut.removeModuleState(moduleState)

        assertTrue { model.modules.isEmpty() }
    }

    @Test
    fun removeModule_RemoveModule() {
        val module = WorkbenchModule<String>(moduleType,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ="title", model = "model", module = module, displayType = displayType){}
        model.modules += moduleState
        assertTrue { model.modules.contains(moduleState) }

        sut.removeModuleState(moduleState)
        assertTrue { model.modules.isEmpty() }
    }

    @Test
    fun convertToWindow() {
        val module = WorkbenchModule<String>(moduleType,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ="title", model = "model", module = module, displayType = displayType){}

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