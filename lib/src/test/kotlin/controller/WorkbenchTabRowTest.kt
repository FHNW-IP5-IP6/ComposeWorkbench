package controller

import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchTabRowTest {

    private var sut = WorkbenchController
    private val displayType = DisplayType.LEFT
    private val moduleType = ModuleType.EXPLORER

    private var tabRowKey = TabRowKey(displayType, moduleType, sut.informationState.mainWindow)

    @BeforeEach
    fun setup(){
        sut.resetInformationState()
        sut.setAppTitle("appTitle")
        tabRowKey = TabRowKey(displayType, moduleType, sut.informationState.mainWindow)
    }

    @Test
    fun removeModule_NoModules() {
        assertEquals(0, sut.informationState.getModulesFiltered(tabRowKey).size)
        sut.removeModuleState(getNewModuleState())

        assertEquals(0, sut.informationState.getModulesFiltered(tabRowKey).size)
    }

    @Test
    fun removeModule_RemoveModule() {
        val moduleState = getNewModuleState()
        sut.addModuleState(moduleState)
        assertTrue { sut.informationState.getModulesFiltered(tabRowKey).contains(moduleState) }
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))

        sut.removeModuleState(moduleState)
        assertEquals(0, sut.informationState.getModulesFiltered(tabRowKey).size)
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.informationState.getIndex(null, tabRowKey))
    }

    @Test
    fun getIndex_OneExplorers() {
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model", displayType = displayType)

        assertEquals(0, sut.informationState.getIndex(explorer.id, TabRowKey(explorer)))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model", displayType = displayType)
        val explorer2 = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model", displayType = DisplayType.BOTTOM)

        assertEquals(0, sut.informationState.getIndex(explorer1.id, TabRowKey(explorer1)))
        assertEquals(0, sut.informationState.getIndex(explorer2.id, TabRowKey(explorer2)))
    }

    @Test
    fun getIndex_ExplorerInList() {
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model1", displayType = displayType)
        val explorer2 = sut.requestExplorerState(id = 2, modelType = "String", explorerController = "model2", displayType = displayType)

        assertEquals(0, sut.informationState.getIndex(explorer2.id, TabRowKey(explorer2)))
        assertEquals(1, sut.informationState.getIndex(explorer1.id, TabRowKey(explorer1)))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model1", displayType = displayType)
        sut.requestExplorerState(id = 2, modelType = "String", explorerController = "model2", displayType = displayType)

        assertFalse { sut.informationState.getSelectedModule(TabRowKey(explorer1)) == explorer1 }
        sut.moduleStateSelectorPressed(TabRowKey(explorer1), explorer1)
        assertEquals(explorer1, sut.informationState.getSelectedModule(TabRowKey(explorer1)))
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model1", displayType = displayType)
        val explorer2 = sut.requestExplorerState(id = 2, modelType = "String", explorerController = "model2", displayType = displayType)

        assertEquals(explorer2, sut.informationState.getSelectedModule(tabRowKey))
        sut.moduleStateSelectorPressed(tabRowKey, explorer1)
        assertEquals(explorer1, sut.informationState.getSelectedModule(tabRowKey))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer1 = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model", displayType = displayType)

        sut.moduleStateSelectorPressed(tabRowKey, explorer1)
        assertNull(sut.informationState.getSelectedModule(tabRowKey))
    }

    @OptIn(ExperimentalSplitPaneApi::class)
    @Test
    fun setShowAndHideDrawer() {
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        val explorer = sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model", displayType = displayType)

        assertTrue { sut.informationState.leftSplitState.moveEnabled }
        assertEquals( 0.25f, sut.informationState.leftSplitState.positionPercentage )

        sut.moduleStateSelectorPressed(tabRowKey, explorer)
        assertNull(sut.informationState.getSelectedModule(tabRowKey))
        assertFalse { sut.informationState.leftSplitState.moveEnabled }
        assertEquals( 0f, sut.informationState.leftSplitState.positionPercentage )
    }

    @Test
    fun reselectState_StateIsSelected() {
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model1", displayType = displayType)
        val moduleState2 = sut.requestExplorerState(id = 2, modelType = "String", explorerController = "model2", displayType = displayType)
        val moduleState3 = sut.requestExplorerState(id = 3, modelType = "String", explorerController = "model3", displayType = displayType)
        assertEquals(moduleState3, sut.informationState.getSelectedModule(tabRowKey))

        sut.moduleStateSelectorPressed(tabRowKey,moduleState2)
        assertEquals(moduleState2, sut.informationState.getSelectedModule(tabRowKey))
        sut.reselect(moduleState2)
        assertEquals(moduleState3, sut.informationState.getSelectedModule(tabRowKey))
    }

    @Test
    fun reselectState_StateNotSelected() {
        sut.registerExplorer(moduleType = "String", explorer = getModule())
        sut.requestExplorerState(id = 1, modelType = "String", explorerController = "model1", displayType = displayType)
        val moduleState2 = sut.requestExplorerState(id = 2, modelType = "String", explorerController = "model2", displayType = displayType)
        val moduleState3 = sut.requestExplorerState(id = 3, modelType = "String", explorerController = "model3", displayType = displayType)
        assertEquals(moduleState3, sut.informationState.getSelectedModule(tabRowKey))

        sut.reselect(moduleState2)
        assertEquals(moduleState3, sut.informationState.getSelectedModule(tabRowKey))
    }

    private fun getNewModuleState(): WorkbenchModuleState<*> {
        val module = getModule()
        return WorkbenchModuleState(id = 1, controller = "model", module = module, displayType = displayType, window = sut.informationState.mainWindow)
    }

    private fun getModule() :WorkbenchModule<String> {
        return WorkbenchModule(moduleType,"type", title ={"title"}) {_->}
    }

}