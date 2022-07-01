package controller

import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import model.state.WorkbenchWindowState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {"model"} ) {}
        sut.registerEditor("type", editorModule)
        val moduleState = sut.requestEditorState<String>("type", 666)

        val displayController = sut.getDisplayController(moduleState.displayType, ModuleType.EDITOR)
        assertEquals(1, displayController.getModulesFiltered().size)
        assertEquals(moduleState, displayController.getSelectedModule())
        assertEquals(1, sut.getRegisteredEditors<String>("type").size)
        assertEquals(editorModule, sut.getRegisteredEditors<String>("type").first())
    }

    @Test
    fun registerAndRequestExplorer(){
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) {}
        sut.registerExplorer("type", explorerModule)
        val moduleState = sut.requestExplorerState(id = 0, moduleType = "type", explorerModel = "model1", DisplayType.LEFT)

        val displayController = sut.getDisplayController(moduleState.displayType, ModuleType.EXPLORER)
        assertEquals(1, displayController.getModulesFiltered().size)
        assertEquals(moduleState, displayController.getSelectedModule())
        assertEquals(explorerModule, sut.getRegisteredExplorer<String>("type"))
    }

    @Test
    fun convertEditorToWindow() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {"model"} ) {}
        sut.registerEditor("type", editorModule)
        sut.requestEditorState<String>("type", 666)

        val displayController = sut.getDisplayController(DisplayType.TAB1, ModuleType.EDITOR)
        assertEquals(1, displayController.getModulesFiltered().size)
        assertNotNull(displayController.getSelectedModule())

        val moduleState = displayController.getSelectedModule()!!
        val window = sut.moduleToWindow(moduleState)
        assertFalse { displayController.getModulesFiltered().contains(moduleState) }

        val windowDisplayController = sut.getDisplayController(window)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, windowDisplayController.getModulesFiltered().size)
        assertEquals(moduleState, windowDisplayController.getSelectedModule())
        assertEquals(1, sut.getWindows().size)
    }

    @Test
    fun convertExplorerToWindow() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER,"type", title ={"title"}) {}
        sut.registerExplorer("type", explorerModule)
        sut.requestExplorerState<String>( 0,"type", "model", DisplayType.LEFT)

        val displayController = sut.getDisplayController(DisplayType.LEFT, ModuleType.EXPLORER)
        assertEquals(1, displayController.getModulesFiltered().size)
        assertNotNull(displayController.getSelectedModule())

        val moduleState = displayController.getSelectedModule()!!
        val window = sut.moduleToWindow(moduleState)
        assertFalse { displayController.getModulesFiltered().contains(moduleState) }
        assertFalse { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }

        val windowDisplayController = sut.getDisplayController(window)
        assertEquals(DisplayType.WINDOW, moduleState.displayType)
        assertEquals(1, windowDisplayController.getModulesFiltered().size)
        assertEquals(moduleState, windowDisplayController.getSelectedModule())
        assertEquals(1, sut.getWindows().size)
    }

    @Test
    fun handleEmptyTabRow() {
        val displayController = sut.getDisplayController(DisplayType.LEFT, ModuleType.EXPLORER)
        assertTrue { displayController.getModulesFiltered().isEmpty() }
        assertTrue { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }

        sut.handleEmptyTabRow(displayController = displayController)
        assertFalse { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }
    }

    @Test
    fun handleEmptyTabRowWindow() {
        val windowState = WorkbenchWindowState()
        sut.getWindows().add(windowState)
        val displayController = sut.getDisplayController(windowState)
        assertEquals(1, sut.getWindows().size)
        assertTrue { displayController.getModulesFiltered().isEmpty() }
        assertTrue { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }

        sut.handleEmptyTabRow(displayController = displayController)
        assertFalse { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }
        assertTrue { sut.getWindows().isEmpty() }
    }

    @Test
    fun handleEmptyTabRowExplorerTab2() {
        sut.changeSplitViewMode(SplitViewMode.VERTICAL)
        val displayController = sut.getDisplayController(DisplayType.TAB2, ModuleType.EDITOR)
        assertTrue { displayController.getModulesFiltered().isEmpty() }
        assertTrue { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }

        sut.handleEmptyTabRow(displayController = displayController)
        assertFalse { sut.displayControllers.containsKey(DisplayControllerKey(displayController)) }
    }

    @Test
    fun handleEmptyTabRowExplorerTab1() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {"model $it" } ) {}
        sut.registerEditor("type", editorModule)
        val moduleState1 = sut.requestEditorState<String>("type", 666)
        val moduleState2 = sut.requestEditorState<String>("type", 777)
        sut.changeSplitViewMode(SplitViewMode.VERTICAL)
        moduleState2.displayType = DisplayType.TAB2
        moduleState1.displayType = DisplayType.TAB2

        val tab1 = sut.getDisplayController(DisplayType.TAB1, ModuleType.EDITOR)
        val tab2 = sut.getDisplayController(DisplayType.TAB2, ModuleType.EDITOR)

        assertEquals(2, tab2.getModulesFiltered().size)
        assertTrue { tab1.getModulesFiltered().isEmpty() }

        val selected = tab2.getSelectedModule()
        sut.handleEmptyTabRow(displayController = tab1)
        assertFalse { sut.displayControllers.containsKey(DisplayControllerKey(tab2)) }
        assertTrue { sut.displayControllers.containsKey(DisplayControllerKey(tab1)) }
        assertEquals(DisplayType.TAB1, moduleState1.displayType)
        assertEquals(DisplayType.TAB1, moduleState2.displayType)
        assertEquals(selected, tab1.getSelectedModule())
    }

}