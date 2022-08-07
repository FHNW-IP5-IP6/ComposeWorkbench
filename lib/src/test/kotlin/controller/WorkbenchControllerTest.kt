package controller

import Workbench
import failure
import model.data.Command
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.PopUpType
import model.state.WorkbenchModuleState
import org.jetbrains.compose.splitpane.ExperimentalSplitPaneApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import success
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

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

        sut.executeAction(WorkbenchAction.ShowDrawer(DisplayType.BOTTOM))
        assertEquals(0.7f, sut.informationState.bottomSplitState.positionPercentage)
        sut.executeAction(WorkbenchAction.ShowDrawer(DisplayType.LEFT))
        assertEquals(0.25f, sut.informationState.leftSplitState.positionPercentage)

        sut.executeAction(WorkbenchAction.HideDrawer(DisplayType.BOTTOM))
        assertEquals(1f, sut.informationState.bottomSplitState.positionPercentage)
        sut.executeAction(WorkbenchAction.HideDrawer(DisplayType.LEFT))
        assertEquals(0f, sut.informationState.leftSplitState.positionPercentage)
    }

    @Test
    fun getNextKey() {
        assertFalse { sut.getNextKey() == sut.getNextKey() }
    }

    @Test
    fun getAppTitle() {
        sut.executeAction(WorkbenchAction.SetAppTitle("appTitle"))

        assertEquals("appTitle", sut.informationState.appTitle)
    }

    @Test
    fun registerAndRequestEditor(){
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"} ) {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 666))
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
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))
        val moduleState = WorkbenchModuleState(module =  explorerModule, controller = "bla", displayType = DisplayType.LEFT, id = 456, window = sut.informationState.mainWindow)
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(moduleState))

        val tabRowKey = TabRowKey(displayType = moduleState.displayType, moduleType = ModuleType.EXPLORER, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertEquals(moduleState, sut.informationState.getSelectedModule(tabRowKey))
        assertEquals(explorerModule, sut.informationState.getRegisteredExplorer("type"))
    }

    @Test
    fun convertEditorToWindow() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"} ) {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 666))

        val tabRowKey = TabRowKey(displayType = DisplayType.TAB1, moduleType = ModuleType.EDITOR, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.informationState.getSelectedModule(tabRowKey))

        val moduleState = sut.informationState.getSelectedModule(tabRowKey)!!
        sut.executeAction(WorkbenchAction.ModuleToWindow(moduleState))
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
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(moduleState))

        val tabRowKey = TabRowKey(displayType = DisplayType.LEFT, moduleType = ModuleType.EXPLORER, windowState = sut.informationState.mainWindow)
        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKey).size)
        assertNotNull(sut.informationState.getSelectedModule(tabRowKey))

        val selected = sut.informationState.getSelectedModule(tabRowKey)!!
        sut.executeAction(
            WorkbenchAction.ModuleToWindow(selected))
        assertFalse { sut.informationState.getModulesFiltered(tabRowKey).contains(selected) }

        val tabRowKeyWindow = TabRowKey(displayType = DisplayType.WINDOW, moduleType = ModuleType.BOTH, windowState = sut.informationState.windows[0])

        assertEquals(1, sut.informationState.getModulesFiltered(tabRowKeyWindow).size)
        val newModule = sut.informationState.getModulesFiltered(tabRowKeyWindow).first()
        assertEquals(DisplayType.WINDOW, newModule.displayType)
        assertEquals(1, sut.informationState.windows.size)
    }

    @Test
    fun saveAndClose() {
        var saved = false
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"},
            onSave = {_, _ ->
                saved = true
                success()
            }
        )  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        val moduleState = sut.informationState.modules[0]

        sut.executeAction(WorkbenchAction.OpenPopUp(PopUpType.ON_CLOSE, moduleState, editorModule, "test message"))
        val popupState = sut.informationState.popUpList.first()
        assertTrue { sut.informationState.isShowPopUp() }

        sut.executeAction(WorkbenchAction.SaveAndClose(moduleState = moduleState, popupState))

        assertFalse { sut.informationState.isShowPopUp() }
        assertTrue { sut.informationState.modules.isEmpty() }
        assertTrue { saved }
    }

    @Test
    fun saveAndCloseTestError() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"},
            onSave = {_, _ ->
                failure("Test Error")
            }
        )  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        val moduleState = sut.informationState.modules[0]

        sut.executeAction(WorkbenchAction.OpenPopUp(PopUpType.ON_CLOSE, moduleState, editorModule, "test message"))
        val popupState = sut.informationState.popUpList.first()
        assertTrue { sut.informationState.isShowPopUp() }

        sut.executeAction(WorkbenchAction.SaveAndClose(moduleState = moduleState, popupState))

        assertTrue { sut.informationState.isShowPopUp() }
        //TODO: Would expect the Module to stay if save is not successful
        //assertTrue { sut.informationState.modules.contains(moduleState) }
        assertEquals(PopUpType.SAVE_FAILED, sut.informationState.popUpList.first().type)
    }

    @Test
    fun discard() {
        var saved = false
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"},
            onSave = {_, _ ->
                saved = true
                success()
            }
        )  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        val moduleState = sut.informationState.modules[0]

        sut.executeAction(WorkbenchAction.OpenPopUp(PopUpType.ON_CLOSE, moduleState, editorModule, "test message"))
        val popupState = sut.informationState.popUpList.first()
        assertTrue { sut.informationState.isShowPopUp() }

        sut.executeAction(WorkbenchAction.DiscardChanges(moduleState = moduleState, popupState))

        assertFalse { sut.informationState.isShowPopUp() }
        assertTrue { sut.informationState.modules.isEmpty() }
        assertFalse { saved }
    }

    @Test
    fun addCommand() {
        val command = Command(text = "Save All", action = WorkbenchAction.SaveAll(), paths = mutableListOf("path"))
        sut.executeAction(WorkbenchActionSync.AddCommand(command))

        assertEquals(command, sut.informationState.commands.last())
    }

    @Test
    fun updateCurrentTabSpace() {
        assertEquals(DisplayType.TAB1, sut.informationState.currentTabSpace)
        sut.executeAction(WorkbenchAction.UpdateCurrentTabSpace(DisplayType.TAB2))
        assertEquals(DisplayType.TAB2, sut.informationState.currentTabSpace)
    }

    @Test
    fun savedState() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"})  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        val moduleState = sut.informationState.modules[0]

        sut.executeAction(WorkbenchAction.AddUnsavedModule("type", 23))
        assertTrue { sut.informationState.isUnsaved(moduleState) }

        sut.executeAction(WorkbenchAction.SaveAll())
        assertFalse { sut.informationState.isUnsaved(moduleState) }
    }

    @Test
    fun closeAll() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"})  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 24))

        val moduleState1 = sut.informationState.modules[0]
        val moduleState2 = sut.informationState.modules[0]

        sut.executeAction(WorkbenchAction.CloseAll(sut.informationState.mainWindow))
        assertFalse { sut.informationState.modules.contains(moduleState1) }
        assertFalse { sut.informationState.modules.contains(moduleState2) }
    }

    @Test
    fun closeAllWithUnsavedState() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"})  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 24))

        val moduleState1 = sut.informationState.modules[0]
        val moduleState2 = sut.informationState.modules[1]

        sut.executeAction(WorkbenchAction.AddUnsavedModule("type", 23))
        sut.executeAction(WorkbenchAction.CloseAll(sut.informationState.mainWindow))
        assertTrue { sut.informationState.modules.contains(moduleState1) }
        assertTrue { sut.informationState.isShowPopUp() }
        assertFalse { sut.informationState.modules.contains(moduleState2) }
    }
}