package controller

import Workbench
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.data.enums.SplitViewMode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchSplitviewTest {

    private var wb = Workbench()
    private var sut = wb.getController()

    @BeforeEach
    fun setup(){
        wb = Workbench()
        sut = wb.getController()
    }

    @Test
    fun changeSplitViewModeTestNoChange() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"})  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 24))

        val informationStateBefore = sut.informationState
        assertEquals(SplitViewMode.UNSPLIT, sut.informationState.splitViewMode)
        sut.executeAction(WorkbenchAction.ChangeSplitViewMode(SplitViewMode.UNSPLIT))
        assertEquals(SplitViewMode.UNSPLIT, sut.informationState.splitViewMode)
        assertEquals(informationStateBefore, sut.informationState)
    }

    @Test
    fun changeSplitViewModeTestSplitAndUnsplit() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"})  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 24))

        val tb1 = TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, sut.informationState.mainWindow)
        val tb2 = TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, sut.informationState.mainWindow)

        assertEquals(24 ,sut.informationState.getSelectedModule(tb1)?.dataId)
        assertEquals(SplitViewMode.UNSPLIT, sut.informationState.splitViewMode)

        //Split Editor Section
        sut.executeAction(WorkbenchAction.ChangeSplitViewMode(SplitViewMode.HORIZONTAL))

        assertEquals(SplitViewMode.HORIZONTAL, sut.informationState.splitViewMode)
        assertEquals(24 ,sut.informationState.getSelectedModule(tb2)?.dataId)
        assertEquals(23 ,sut.informationState.getSelectedModule(tb1)?.dataId)
        assertEquals(DisplayType.TAB2 ,sut.informationState.currentTabSpace)

        //Split in other direction
        sut.executeAction(WorkbenchAction.ChangeSplitViewMode(SplitViewMode.VERTICAL))

        assertEquals(SplitViewMode.VERTICAL, sut.informationState.splitViewMode)
        assertEquals(24 ,sut.informationState.getSelectedModule(tb2)?.dataId)
        assertEquals(23 ,sut.informationState.getSelectedModule(tb1)?.dataId)
        assertEquals(DisplayType.TAB2 ,sut.informationState.currentTabSpace)

        //Back to unsplit
        sut.executeAction(WorkbenchAction.ChangeSplitViewMode(SplitViewMode.UNSPLIT))

        assertEquals(SplitViewMode.UNSPLIT, sut.informationState.splitViewMode)
        assertEquals(23 ,sut.informationState.getSelectedModule(tb1)?.dataId)
        assertEquals(DisplayType.TAB1 ,sut.informationState.currentTabSpace)
        assertNull(sut.informationState.getSelectedModule(tb2))
        sut.informationState.modules.forEach { assertTrue { it.displayType == DisplayType.TAB1 } }
    }
}