package controller

import Workbench
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
import kotlin.test.assertNotNull

class WorkbenchControllerTest {

    private var wb = Workbench()
    private var sut = wb.getController()

    @BeforeEach
    fun setup(){
        wb = Workbench()
        sut = wb.getController()
    }

    @OptIn(ExperimentalSplitPaneApi::class)
    @Test
    fun drawer() {
        assertEquals(1f, sut.informationState.bottomSplitState.positionPercentage)
        assertEquals(0f, sut.informationState.leftSplitState.positionPercentage)

        sut.triggerAction(WorkbenchAction.ShowDrawer(DisplayType.BOTTOM))
        assertEquals(0.7f, sut.informationState.bottomSplitState.positionPercentage)
        sut.triggerAction(WorkbenchAction.ShowDrawer(DisplayType.LEFT))
        assertEquals(0.25f, sut.informationState.leftSplitState.positionPercentage)

        sut.triggerAction(WorkbenchAction.HideDrawer(DisplayType.BOTTOM))
        assertEquals(1f, sut.informationState.bottomSplitState.positionPercentage)
        sut.triggerAction(WorkbenchAction.HideDrawer(DisplayType.LEFT))
        assertEquals(0f, sut.informationState.leftSplitState.positionPercentage)
    }

    @Test
    fun getNextKey() {
        assertFalse { sut.getNextKey() == sut.getNextKey() }
    }

    @Test
    fun getAppTitle() {
        sut.triggerAction(WorkbenchAction.SetAppTitle("appTitle"))

        assertEquals("appTitle", sut.informationState.appTitle)
    }

    @Test
    fun registerAndRequestEditor(){
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {id, mqtt -> "model"} ) {}
        sut.triggerAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.triggerAction(WorkbenchActionSync.RequestEditorState("type", 666))
        val moduleState = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 666 }!!

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EDITOR, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))
        assertEquals(1, sut.informationState.getRegisteredEditors<String>("type").size)
        assertEquals(editorModule, sut.informationState.getRegisteredEditors<String>("type").first())
    }

    @Test
    fun registerAndRequestExplorer(){
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) {}
        sut.triggerAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))
        val moduleState = WorkbenchModuleState<String>(module =  explorerModule, controller = "bla", displayType = DisplayType.LEFT, id = 456, window = sut.informationState.mainWindow)
        sut.triggerAction(WorkbenchActionSync.RequestExplorerState(moduleState))

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EXPLORER, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))
        assertEquals(explorerModule, sut.informationState.getRegisteredExplorer<String>("type"))
    }

    @Test
    fun convertEditorToWindow() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {id, mqtt -> "model"} ) {}
        sut.triggerAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.triggerAction(WorkbenchActionSync.RequestEditorState("type", 666))

        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.informationState.getSelectedModule(tabRowKey))

        val moduleState = sut.informationState.getSelectedModule(tabRowKey)!!
        sut.triggerAction(WorkbenchAction.ModuleToWindow(moduleState))
        assertFalse { sut.informationState.getModulesFiltered(tabRowKey).contains(moduleState) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = sut.informationState.windows[0])
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKeyWindow).size)
        val newModule = sut.informationState.getModulesFiltered(tabRowKeyWindow).first()
        assertEquals(DisplayType.WINDOW, newModule.displayType)
        assertEquals(1, sut.informationState.windows.size)
    }

    @Test
    fun convertExplorerToWindow() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) { }
        val moduleState = WorkbenchModuleState(module =  explorerModule, controller = "bla", displayType = DisplayType.LEFT, id = 456, window = sut.informationState.mainWindow)
        sut.triggerAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))
        sut.triggerAction(WorkbenchActionSync.RequestExplorerState(moduleState))

        val tabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.informationState.getSelectedModule(tabRowKey))

        val selected = sut.informationState.getSelectedModule(tabRowKey)!!
        sut.triggerAction(
            WorkbenchAction.ModuleToWindow(selected))
        assertFalse { sut.informationState.getModulesFiltered(tabRowKey).contains(selected) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = sut.informationState.windows[0])

        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKeyWindow).size)
        val newModule = sut.informationState.getModulesFiltered(tabRowKeyWindow).first()
        assertEquals(DisplayType.WINDOW, newModule.displayType)
        assertEquals(1, sut.informationState.windows.size)
    }
}