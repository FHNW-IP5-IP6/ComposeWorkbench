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

    private var sut = WorkbenchController("appTitle")

    @BeforeEach
    fun setup(){
        sut = WorkbenchController("appTitle")
    }

    @Test
    fun getNextKey() {
        assertFalse { sut.getNextKey() == sut.getNextKey() }
    }

    @Test
    fun getAppTitle() {
        assertEquals("appTitle", sut.getAppTitle())
    }

    @Test
    fun registerAndRequestEditor(){
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {c, m -> "model"} ) {_,_->}
        sut.registerEditor("type", editorModule)
        val moduleState = sut.requestEditorState<String>("type", 666)

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EDITOR, windowState = sut.getMainWindow())
        assertEquals(1, sut.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.getSelectedModule(tabRowKey))
        assertEquals(1, sut.getRegisteredEditors<String>("type").size)
        assertEquals(editorModule, sut.getRegisteredEditors<String>("type").first())
    }

    @Test
    fun registerAndRequestExplorer(){
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) {_,_->}
        sut.registerExplorer("type", explorerModule)
        val moduleState = sut.requestExplorerState(id = 0, moduleType = "type", explorerController = "model1", DisplayType.LEFT)

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EXPLORER, windowState = sut.getMainWindow())
        assertEquals(1, sut.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.getSelectedModule(tabRowKey))
        assertEquals(explorerModule, sut.getRegisteredExplorer<String>("type"))
    }

    @Test
    fun convertEditorToWindow() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {c, m -> "model"} ) {_,_->}
        sut.registerEditor("type", editorModule)
        sut.requestEditorState<String>("type", 666)

        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.getMainWindow())
        assertEquals(1, sut.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.getSelectedModule(tabRowKey))

        val moduleState = sut.getSelectedModule(tabRowKey)!!
        val window = sut.moduleToWindow(moduleState)
        assertFalse { sut.getModulesFiltered(tabRowKey).contains(moduleState) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = window)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, sut.getModulesFiltered(tabRowKeyWindow).size)
        assertEquals(moduleState, sut.getSelectedModule(tabRowKeyWindow))
        assertEquals(1, sut.informationState.windows.size)
    }

    @Test
    fun convertExplorerToWindow() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) {_,_->}
        sut.registerExplorer("type", explorerModule)
        sut.requestExplorerState<String>( 0,"type", "model", DisplayType.LEFT)

        val tabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = sut.getMainWindow())
        assertEquals(1, sut.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.getSelectedModule(tabRowKey))

        val moduleState = sut.getSelectedModule(tabRowKey)!!
        val window = sut.moduleToWindow(moduleState)
        assertFalse { sut.getModulesFiltered(tabRowKey).contains(moduleState) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = window)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, sut.getModulesFiltered(tabRowKeyWindow).size)
        assertEquals(moduleState, sut.getSelectedModule(tabRowKeyWindow))
        assertEquals(1, sut.informationState.windows.size)
    }
}