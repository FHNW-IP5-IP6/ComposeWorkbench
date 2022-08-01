package controller

import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class WorkbenchControllerTest {

    private var sut = WorkbenchController

    @BeforeEach
    fun setup(){
        sut.resetInformationState()
        sut.setAppTitle("appTitle")
    }

    @Test
    fun drawer() {
        assertEquals(1f, sut.informationState.bottomSplitState.positionPercentage)
        assertEquals(0f, sut.informationState.leftSplitState.positionPercentage)
        var informationState = sut.informationState

        informationState = sut.showDrawer(informationState, DisplayType.BOTTOM)
        assertEquals(0.7f, informationState.bottomSplitState.positionPercentage)
        informationState = sut.showDrawer(informationState, DisplayType.LEFT)
        assertEquals(0.25f, informationState.leftSplitState.positionPercentage)

        informationState = sut.hideDrawer(informationState, DisplayType.BOTTOM)
        assertEquals(1f, informationState.bottomSplitState.positionPercentage)
        informationState = sut.hideDrawer(informationState, DisplayType.LEFT)
        assertEquals(0f, informationState.leftSplitState.positionPercentage)
    }

    @Test
    fun getNextKey() {
        assertFalse { sut.getNextKey() == sut.getNextKey() }
    }

    @Test
    fun getAppTitle() {
        assertEquals("appTitle", sut.informationState.appTitle)
    }

    @Test
    fun registerAndRequestEditor(){
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {id, mqtt -> "model"} ) {}
        sut.registerEditor("type", editorModule)
        val moduleState = sut.requestEditorState<String>("type", 666)

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EDITOR, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))
        assertEquals(1, sut.informationState.getRegisteredEditors<String>("type").size)
        assertEquals(editorModule, sut.informationState.getRegisteredEditors<String>("type").first())
    }

    @Test
    fun registerAndRequestExplorer(){
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) {}
        sut.registerExplorer("type", explorerModule)
        val moduleState = sut.requestExplorerState(id = 0, modelType = "type", explorerController = "model1", DisplayType.LEFT)

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EXPLORER, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))
        assertEquals(explorerModule, sut.informationState.getRegisteredExplorer<String>("type"))
    }

    @Test
    fun convertEditorToWindow() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {id, mqtt -> "model"} ) {}
        sut.registerEditor("type", editorModule)
        sut.requestEditorState<String>("type", 666)

        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.informationState.getSelectedModule(tabRowKey))

        val moduleState = sut.informationState.getSelectedModule(tabRowKey)!!
        val window = sut.moduleToWindow(moduleState)
        assertFalse { sut.informationState.getModulesFiltered(tabRowKey).contains(moduleState) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = window)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKeyWindow).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKeyWindow))
        assertEquals(1, sut.informationState.windows.size)
    }

    @Test
    fun convertExplorerToWindow() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) { }
        sut.registerExplorer("type", explorerModule)
        sut.requestExplorerState<String>( 0,"type", "model", DisplayType.LEFT)

        val tabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.informationState.getSelectedModule(tabRowKey))

        val moduleState = sut.informationState.getSelectedModule(tabRowKey)!!
        val window = sut.moduleToWindow(moduleState)
        assertFalse { sut.informationState.getModulesFiltered(tabRowKey).contains(moduleState) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = window)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKeyWindow).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKeyWindow))
        assertEquals(1, sut.informationState.windows.size)
    }
}