package controller

import model.data.DisplayType
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkbenchControllerTest {

    private var sut = WorkbenchController("appTitle")

    @Test
    fun getNextKey() {
        assertFalse { sut.getNextKey() == sut.getNextKey() }
    }

    @Test
    fun getAppTitle() {
        assertEquals("appTitle", sut.getAppTitle())
    }

    @Test
    fun convertToWindow() {
        val module = WorkbenchModule<String>(ModuleType.EDITOR,"type", title ={"title"}) {}
        val moduleState = WorkbenchModuleState(id = 0, model = "model", module = module, displayType = DisplayType.LEFT)
        sut.selectionController.addModuleState(moduleState)
        val displayController = sut.createModuleDisplayController(DisplayType.LEFT, ModuleType.EDITOR)

        sut.selectionController.addModuleState(moduleState)
        assertTrue { displayController.getModulesFiltered().contains(moduleState) }

        val window = sut.moduleToWindow(moduleState)
        assertFalse { displayController.getModulesFiltered().contains(moduleState) }

        assertEquals(DisplayType.WINDOW, window.modules[0].displayType)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, sut.getWindows().size)
    }

}