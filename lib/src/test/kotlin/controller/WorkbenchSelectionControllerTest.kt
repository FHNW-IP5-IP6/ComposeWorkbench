package controller

import model.data.DisplayType
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WorkbenchSelectionControllerTest {

    private var controller = WorkbenchController("title")
    private var sut = controller.selectionController

    @BeforeEach
    fun setup(){
        controller = WorkbenchController("title")
        sut = controller.selectionController
    }

    @Test
    fun setShowAndHideDrawer() {
        val module = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title = {"title"}) {}
        val moduleState = WorkbenchModuleState(id = 1, model = "model", module = module, displayType = DisplayType.LEFT)
        sut.addModuleState(moduleState)

        sut.setSelectedModule(moduleState)
        assertTrue { controller.getLeftSplitState().moveEnabled }
        assertEquals( 0.25f, controller.getLeftSplitState().positionPercentage )

        sut.setSelectedModuleNull(DisplayType.LEFT, moduleType = ModuleType.EXPLORER)
        assertFalse { controller.getLeftSplitState().moveEnabled }
        assertEquals( 0f, controller.getLeftSplitState().positionPercentage )
    }

    @Test
    fun reselectState_StateIsSelected() {
        val module = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title = {"title"}) {}
        val moduleState1 = WorkbenchModuleState(id = 1, model = "model", module = module, displayType = DisplayType.LEFT)
        val moduleState2 = WorkbenchModuleState(id = 2, model = "model1", module = module, displayType = DisplayType.LEFT)
        val moduleState3 = WorkbenchModuleState(id = 3, model = "model2", module = module, displayType = DisplayType.LEFT)
        sut.addModuleState(moduleState1)
        sut.addModuleState(moduleState2)
        sut.addModuleState(moduleState3)

        sut.setSelectedModule(moduleState2)
        assertEquals(moduleState2, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )
        sut.reselectState(moduleState2)
        assertEquals(moduleState3, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )
    }

    @Test
    fun reselectState_StateNotSelected() {
        val module = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title = {"title"}) {}
        val moduleState1 = WorkbenchModuleState(id = 1, model = "model", module = module, displayType = DisplayType.LEFT)
        val moduleState2 = WorkbenchModuleState(id = 2, model = "model1", module = module, displayType = DisplayType.LEFT)
        val moduleState3 = WorkbenchModuleState(id = 3, model = "model2", module = module, displayType = DisplayType.LEFT)
        sut.addModuleState(moduleState1)
        sut.addModuleState(moduleState2)
        sut.addModuleState(moduleState3)

        sut.setSelectedModule(moduleState3)
        assertEquals(moduleState3, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )
        sut.reselectState(moduleState2)
        assertEquals(moduleState3, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )
    }
}