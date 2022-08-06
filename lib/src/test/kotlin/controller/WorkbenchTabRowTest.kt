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

    private var sut = WorkbenchController()
    private val displayType = DisplayType.LEFT
    private val moduleType = ModuleType.EXPLORER

    private var tabRowKey = TabRowKey(displayType, moduleType, sut.informationState.mainWindow)

    @BeforeEach
    fun setup(){
        sut = WorkbenchController()
        sut.executeAction(WorkbenchAction.SetAppTitle("appTitle"))
        tabRowKey = TabRowKey(displayType, moduleType, sut.informationState.mainWindow)
    }

    @Test
    fun removeModule_NoModules() {
        assertEquals(0, sut.informationState.getModulesFiltered(tabRowKey).size)
        sut.executeAction(WorkbenchAction.RemoveModuleState(getNewModuleState()))

        assertEquals(0, sut.informationState.getModulesFiltered(tabRowKey).size)
    }

    @Test
    fun removeModule_RemoveModule() {
        val moduleState = getNewModuleState()
        sut.executeAction(WorkbenchAction.AddModuleState(moduleState))
        assertTrue { sut.informationState.getModulesFiltered(tabRowKey).contains(moduleState) }
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))

        sut.executeAction(WorkbenchAction.RemoveModuleState(moduleState))
        assertEquals(0, sut.informationState.getModulesFiltered(tabRowKey).size)
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.informationState.getIndex(null, tabRowKey))
    }

    @Test
    fun getIndex_OneExplorers() {
        val explorer = registerAndRequestEditor(1)

        assertEquals(0, sut.informationState.getIndex(explorer.id, TabRowKey(explorer)))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        val explorer = registerAndRequestEditor(1)

        assertEquals(0, sut.informationState.getIndex(explorer.id, TabRowKey(explorer)))
        assertEquals(0, sut.informationState.getIndex(234, TabRowKey(DisplayType.TAB1, ModuleType.EXPLORER, sut.informationState.mainWindow)))
    }

    @Test
    fun getIndex_ExplorerInList() {
        val explorerModule = getModule()
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("String", explorerModule))
        val explorer1 = WorkbenchModuleState(module =  explorerModule, controller = "c1", displayType = DisplayType.LEFT, id = 456, window = sut.informationState.mainWindow)
        val explorer2 = WorkbenchModuleState(module =  explorerModule, controller = "c2", displayType = DisplayType.LEFT, id = 897, window = sut.informationState.mainWindow)
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer1))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer2))

        assertEquals(0, sut.informationState.getIndex(explorer2.id, TabRowKey(explorer2)))
        assertEquals(1, sut.informationState.getIndex(explorer1.id, TabRowKey(explorer1)))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        val explorer1 = registerAndRequestEditor(1)
        sut.executeAction(WorkbenchAction.UpdateSelection(TabRowKey(explorer1), null))
        assertFalse { sut.informationState.getSelectedModule(TabRowKey(explorer1)) == explorer1 }
        sut.executeAction(WorkbenchAction.TabSelectorPressed(TabRowKey(explorer1), explorer1))
        assertEquals(explorer1, sut.informationState.getSelectedModule(TabRowKey(explorer1)))
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        val explorerModule = getModule()
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("String", explorerModule))
        val explorer1 = WorkbenchModuleState(module =  explorerModule, controller = "c1", displayType = DisplayType.LEFT, id = 456, window = sut.informationState.mainWindow)
        val explorer2 = WorkbenchModuleState(module =  explorerModule, controller = "c2", displayType = DisplayType.LEFT, id = 897, window = sut.informationState.mainWindow)
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer1))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer2))

        assertEquals(explorer2, sut.informationState.getSelectedModule(tabRowKey))
        sut.executeAction(WorkbenchAction.TabSelectorPressed(TabRowKey(explorer1), explorer1))
        assertEquals(explorer1, sut.informationState.getSelectedModule(tabRowKey))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        val explorer1 = registerAndRequestEditor(1)

        sut.executeAction(WorkbenchAction.TabSelectorPressed(TabRowKey(explorer1), explorer1))
        assertNull(sut.informationState.getSelectedModule(tabRowKey))
    }

    @OptIn(ExperimentalSplitPaneApi::class)
    @Test
    fun setShowAndHideDrawer() {
        val explorer = registerAndRequestEditor(1)

        assertTrue { sut.informationState.leftSplitState.moveEnabled }
        assertEquals( 0.25f, sut.informationState.leftSplitState.positionPercentage )

        sut.executeAction(WorkbenchAction.TabSelectorPressed(tabRowKey, explorer))
        assertNull(sut.informationState.getSelectedModule(tabRowKey))
        assertFalse { sut.informationState.leftSplitState.moveEnabled }
        assertEquals( 0f, sut.informationState.leftSplitState.positionPercentage )
    }

    @Test
    fun reselectState_StateIsSelected() {
        val explorerModule = getModule()
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("String", explorerModule))
        val explorer1 = WorkbenchModuleState(module =  explorerModule, controller = "c1", displayType = DisplayType.LEFT, id = 1, window = sut.informationState.mainWindow)
        val explorer2 = WorkbenchModuleState(module =  explorerModule, controller = "c2", displayType = DisplayType.LEFT, id = 2, window = sut.informationState.mainWindow)
        val explorer3 = WorkbenchModuleState(module =  explorerModule, controller = "c3", displayType = DisplayType.LEFT, id = 3, window = sut.informationState.mainWindow)
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer1))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer2))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer3))

        assertEquals(explorer3, sut.informationState.getSelectedModule(tabRowKey))

        sut.executeAction(WorkbenchAction.TabSelectorPressed(tabRowKey, explorer2))
        assertEquals(explorer2, sut.informationState.getSelectedModule(tabRowKey))
        sut.executeAction(WorkbenchAction.ReselectModuleState(explorer2))
        assertEquals(explorer3, sut.informationState.getSelectedModule(tabRowKey))
    }

    @Test
    fun reselectState_StateNotSelected() {
        val explorerModule = getModule()
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("String", explorerModule))
        val explorer1 = WorkbenchModuleState(module =  explorerModule, controller = "c1", displayType = DisplayType.LEFT, id = 1, window = sut.informationState.mainWindow)
        val explorer2 = WorkbenchModuleState(module =  explorerModule, controller = "c2", displayType = DisplayType.LEFT, id = 2, window = sut.informationState.mainWindow)
        val explorer3 = WorkbenchModuleState(module =  explorerModule, controller = "c3", displayType = DisplayType.LEFT, id = 3, window = sut.informationState.mainWindow)
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer1))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer2))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer3))

        assertEquals(explorer3, sut.informationState.getSelectedModule(tabRowKey))

        sut.executeAction(WorkbenchAction.ReselectModuleState(explorer2))
        assertEquals(explorer3, sut.informationState.getSelectedModule(tabRowKey))
    }

    private fun getNewModuleState(): WorkbenchModuleState<*> {
        val module = getModule()
        return WorkbenchModuleState(id = 1, controller = "model", module = module, displayType = displayType, window = sut.informationState.mainWindow)
    }

    private fun getModule() :WorkbenchModule<String> {
        return WorkbenchModule(moduleType,"type", title ={"title"}) {_->}
    }

    private fun registerAndRequestEditor(id: Int, type: String = "type", displayType: DisplayType = DisplayType.LEFT): WorkbenchModuleState<*>{
        val explorerModule = getModule()
        sut.executeAction(WorkbenchActionSync.RegisterExplorer(type, explorerModule))
        val explorer = WorkbenchModuleState(module =  explorerModule, controller = "controller", displayType = displayType, id = id, window = sut.informationState.mainWindow)
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer))
        return explorer
    }

}