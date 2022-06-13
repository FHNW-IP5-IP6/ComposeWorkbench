package model

import model.data.DisplayType
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WorkbenchModelTest {

    private var sut = WorkbenchModel()

    @Test
    fun initialStates() {
        assertEquals( 0, sut.modules.size )
        assertTrue{ sut.registeredExplorers.isEmpty() }
        assertTrue{ sut.registeredEditors.isEmpty() }
    }

    @Test
    fun getNextKey() {
        assertFalse { sut.getNextKey() == sut.getNextKey() }
    }

    @Test
    fun setShowAndHideDrawer() {
        val module = WorkbenchModule<String>(ModuleType.EXPLORER,"type") {}
        val moduleState = WorkbenchModuleState<String>(id = 1, title ={"title"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        sut.modules += moduleState

        sut.setSelectedModule(moduleState)
        assertTrue { sut.leftSplitState.moveEnabled }
        assertEquals( 0.25f, sut.leftSplitState.positionPercentage )

        sut.setSelectedModuleNull(DisplayType.LEFT, moduleType = ModuleType.EXPLORER)
        assertFalse { sut.leftSplitState.moveEnabled }
        assertEquals( 0f, sut.leftSplitState.positionPercentage )
    }

    @Test
    fun reselectState_StateIsSelected() {
        val module = WorkbenchModule<String>(ModuleType.EXPLORER,"type") {}
        val moduleState1 = WorkbenchModuleState<String>(id = 1, title ={"title1"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        val moduleState2 = WorkbenchModuleState<String>(id = 2, title ={"title2"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        val moduleState3 = WorkbenchModuleState<String>(id = 3, title ={"title3"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        sut.modules += moduleState1
        sut.modules += moduleState2
        sut.modules += moduleState3
        sut.setSelectedModule(moduleState2)

        assertEquals(moduleState2, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )

        sut.reselectState(moduleState2)

        assertEquals(moduleState3, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )
    }

    @Test
    fun reselectState_StateNotSelected() {
        val module = WorkbenchModule<String>(ModuleType.EXPLORER,"type") {}
        val moduleState1 = WorkbenchModuleState<String>(id = 1, title ={"title1"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        val moduleState2 = WorkbenchModuleState<String>(id = 2, title ={"title2"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        val moduleState3 = WorkbenchModuleState<String>(id = 3, title ={"title3"}, model = "model", module = module, displayType = DisplayType.LEFT){}
        sut.modules += moduleState1
        sut.modules += moduleState2
        sut.modules += moduleState3
        sut.setSelectedModule(moduleState3)

        assertEquals(moduleState3, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )

        sut.reselectState(moduleState2)

        assertEquals(moduleState3, sut.getSelectedModule(DisplayType.LEFT, ModuleType.EXPLORER).value )
    }
}