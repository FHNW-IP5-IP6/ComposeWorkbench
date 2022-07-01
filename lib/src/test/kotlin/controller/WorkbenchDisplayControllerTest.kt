package controller

import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.WorkbenchModuleState
import model.state.WorkbenchWindowState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchDisplayControllerTest {

    private val displayType = DisplayType.LEFT
    private val moduleType = ModuleType.EXPLORER
    private val window = WorkbenchWindowState()

    private var controller = WorkbenchController("appTitle")
    private var sut = controller.getDisplayController(displayType, moduleType, true)

    @BeforeEach
    fun setup(){
        controller = WorkbenchController("appTitle")
        sut = controller.getDisplayController(displayType, moduleType, true)
    }

    @Test
    fun removeModule_NoModules() {
        assertEquals(0, sut.getModulesFiltered().size)
        sut.removeModuleState(getNewModuleState())

        assertEquals(0, sut.getModulesFiltered().size)
    }

    @Test
    fun removeModule_RemoveModule() {
        val moduleState = getNewModuleState()
        sut.addModuleState(moduleState)
        assertTrue { sut.getModulesFiltered().contains(moduleState) }
        assertEquals(moduleState, sut.getSelectedModule())

        sut.removeModuleState(moduleState)
        assertEquals(0, sut.getModulesFiltered().size)
        assertFalse { controller.displayControllers.containsKey(DisplayControllerKey(moduleState)) }
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.getIndex(null))
    }

    @Test
    fun getIndex_OneExplorers() {
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model", displayType = displayType)

        assertEquals(0, sut.getIndex(explorer))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model", displayType = displayType)
        val explorer2 = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model", displayType = DisplayType.BOTTOM)

        assertEquals(0, sut.getIndex(explorer1))
        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun getIndex_ExplorerInList() {
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model1", displayType = displayType)
        val explorer2 = controller.requestExplorerState(id = 2, moduleType = "String", explorerModel = "model2", displayType = displayType)

        assertEquals(0, sut.getIndex(explorer2))
        assertEquals(1, sut.getIndex(explorer1))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model1", displayType = displayType)
        controller.requestExplorerState(id = 2, moduleType = "String", explorerModel = "model2", displayType = displayType)

        assertFalse { sut.isModuleSelected(explorer1) }
        sut.moduleStateSelectorPressed(explorer1)
        assertTrue { sut.isModuleSelected(explorer1) }
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model1", displayType = displayType)
        val explorer2 = controller.requestExplorerState(id = 2, moduleType = "String", explorerModel = "model2", displayType = displayType)

        assertEquals(explorer2, sut.getSelectedModule())
        sut.moduleStateSelectorPressed(explorer1)
        assertTrue(sut.isModuleSelected(explorer1))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model", displayType = displayType)

        sut.moduleStateSelectorPressed(explorer1)
        assertFalse{ sut.isModuleSelected(explorer1) }
        assertNull(sut.getSelectedModule())
    }

    @Test
    fun setShowAndHideDrawer() {
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model", displayType = displayType)

        assertTrue { controller.getLeftSplitState().moveEnabled }
        assertEquals( 0.25f, controller.getLeftSplitState().positionPercentage )

        sut.setSelectedModuleNull()
        assertFalse { controller.getLeftSplitState().moveEnabled }
        assertEquals( 0f, controller.getLeftSplitState().positionPercentage )
    }

    @Test
    fun reselectState_StateIsSelected() {
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model1", displayType = displayType)
        val moduleState2 = controller.requestExplorerState(id = 2, moduleType = "String", explorerModel = "model2", displayType = displayType)
        val moduleState3 = controller.requestExplorerState(id = 3, moduleType = "String", explorerModel = "model3", displayType = displayType)
        assertEquals(moduleState3, sut.getSelectedModule())

        sut.setSelectedModule(moduleState2)
        assertEquals(moduleState2, sut.getSelectedModule())
        sut.reselectModuleState(moduleState2)
        assertEquals(moduleState3, sut.getSelectedModule())
    }

    @Test
    fun reselectState_StateNotSelected() {
        controller.registerExplorer(moduleType = "String", explorer = getModule())
        controller.requestExplorerState(id = 1, moduleType = "String", explorerModel = "model1", displayType = displayType)
        val moduleState2 = controller.requestExplorerState(id = 2, moduleType = "String", explorerModel = "model2", displayType = displayType)
        val moduleState3 = controller.requestExplorerState(id = 3, moduleType = "String", explorerModel = "model3", displayType = displayType)
        assertEquals(moduleState3, sut.getSelectedModule())

        sut.reselectModuleState(moduleState2)
        assertEquals(moduleState3, sut.getSelectedModule())
    }

    private fun getNewModuleState(): WorkbenchModuleState<*> {
        val module = getModule()
        return WorkbenchModuleState<String>(id = 1, model = "model", module = module, displayType = displayType, window = window)
    }

    private fun getModule() :WorkbenchModule<String> {
        return WorkbenchModule<String>(moduleType,"type", title ={"title"}) {}
    }

}