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
    private val displayType = DisplayType.TAB1
    private val moduleType = ModuleType.EDITOR
    private var model = WorkbenchModel()
    private var sut = WorkbenchModuleController(model, displayType, moduleType)

    @BeforeEach
    fun setup(){
        sut = WorkbenchModuleController(model, displayType, moduleType)
    }

    @Test
    fun removeModule_NoModules() {
        val module = WorkbenchModule<String>(1, moduleType,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ={"title"}, model = "model", module = module, displayType = displayType){}
        sut.removeModuleState(moduleState)

        assertEquals(1, model.modules.size)
    }

    @Test
    fun removeModule_RemoveModule() {
        val module = WorkbenchModule<String>(1, moduleType,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ={"title"}, model = "model", module = module, displayType = displayType){}
        model.modules += moduleState
        assertTrue { model.modules.contains(moduleState) }

        sut.removeModuleState(moduleState)
        assertEquals(1, model.modules.size)
        assertFalse { model.modules.contains(moduleState) }
    }

    @Test
    fun convertToWindow() {
        val module = WorkbenchModule<String>(1, moduleType,"type") {}
        val moduleState = WorkbenchModuleState<String>(title ={"title"}, model = "model", module = module, displayType = displayType){}

        model.modules += moduleState
        assertTrue { model.modules.contains(moduleState) }

        sut.convertToWindow(moduleState)
        assertFalse { model.modules.isEmpty() }
        assertFalse { model.modules.contains(moduleState) }
        assertEquals(2, model.modules.size)
        assertEquals(module, model.modules[1].module)
        assertEquals(DisplayType.WINDOW, model.modules[1].displayType)
    }

}