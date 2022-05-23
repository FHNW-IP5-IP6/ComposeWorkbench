package model

import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class WorkbenchModelTest {

    private var sut = WorkbenchModel()

    @Test
    fun initialStates() {
        assertEquals( 1, sut.modules.size )
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
}