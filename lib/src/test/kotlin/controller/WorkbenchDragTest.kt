package controller

import Workbench
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import model.data.TabRowKey
import model.data.WorkbenchModule
import model.data.enums.DisplayType
import model.data.enums.ModuleType
import model.state.DropTarget
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import util.toDpOffset
import kotlin.test.*

class WorkbenchDragTest {

    private var wb = Workbench()
    private var sut = wb.getController()

    @BeforeEach
    fun setup() {
        wb = Workbench()
        sut = wb.getController()
    }

    @Test
    fun initialState() {
        assertFalse { sut.dragState.isDragging }
        assertNull(sut.dragState.module)
        assertEquals(WindowPosition.PlatformDefault, sut.dragState.dragWindowState.position)
    }

    @Test
    fun reset() {
        val editorModule =
            WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { _, _ -> "model" }) { }
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 1))
        val moduleState = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 1 }!!

        sut.executeAction(DragAndDropAction.StartDragging(moduleState))
        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(20.dp, 10.dp)))

        assertTrue { sut.dragState.isDragging }
        assertEquals(moduleState, sut.dragState.module)
        assertEquals(DpOffset(20.dp, 10.dp), sut.dragState.dragWindowState.position.toDpOffset())

        sut.executeAction(DragAndDropAction.Reset())
        assertFalse { sut.dragState.isDragging }
        assertNull(sut.dragState.module)
    }

    @Test
    fun isValidDropTarget() {
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { }
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))

        val explorerLeft = WorkbenchModuleState(
            module = explorerModule,
            controller = "model1",
            displayType = DisplayType.LEFT,
            id = 1,
            window = sut.informationState.mainWindow
        )
        val explorerBottom = WorkbenchModuleState(
            module = explorerModule,
            controller = "model2",
            displayType = DisplayType.BOTTOM,
            id = 2,
            window = sut.informationState.mainWindow
        )
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorerLeft))
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorerBottom))

        val tabRowKeyBottom = TabRowKey(DisplayType.BOTTOM, ModuleType.EXPLORER, sut.informationState.mainWindow)
        val target = DropTarget(false, tabRowKeyBottom, Rect(0f, 50f, 100f, 100f))

        sut.executeAction(DragAndDropAction.StartDragging(explorerLeft))
        assertTrue { sut.isValidDropTarget(target, explorerLeft) }

        sut.executeAction(DragAndDropAction.StartDragging(explorerBottom))
        assertFalse { sut.isValidDropTarget(target, explorerBottom) }

        sut.executeAction(DragAndDropAction.Reset())
        assertFalse { sut.isValidDropTarget(target, explorerBottom) }
    }

    @Test
    fun dropTargets() {
        val editorModule =
            WorkbenchModule(ModuleType.EDITOR, "type", title = { "title" }, loader = { id, _ -> "model $id" }) { }
        val explorerModule = WorkbenchModule<String>(ModuleType.EXPLORER, "type", title = { "title" }) { }
        sut.executeAction(WorkbenchActionSync.RegisterExplorer("type", explorerModule))
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 1))
        val editor1 = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 1 }!!
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 2))
        val editor2 = sut.informationState.modules.find { it.module.modelType == "type" && it.dataId == 2 }!!
        editor2.displayType = DisplayType.TAB2

        val explorer = WorkbenchModuleState(
            module = explorerModule,
            controller = "model",
            displayType = DisplayType.LEFT,
            id = 1,
            window = sut.informationState.mainWindow
        )
        sut.executeAction(WorkbenchActionSync.RequestExplorerState(explorer))
        val editorTabRowKey1 = TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, sut.informationState.mainWindow)
        val editorTabRowKey2 = TabRowKey(DisplayType.TAB2, ModuleType.EDITOR, sut.informationState.mainWindow)
        val explorerTabRowKey = TabRowKey(DisplayType.LEFT, ModuleType.EXPLORER, sut.informationState.mainWindow)

        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(0.dp, 0.dp)))
        //Only one reverse target (main window)
        sut.executeAction(
            DragAndDropAction.AddDropTarget(
                TabRowKey(
                    DisplayType.WINDOW,
                    ModuleType.BOTH,
                    sut.informationState.mainWindow
                ), Rect(0f, 0f, 100f, 100f),
                true
            )
        )
        assertEquals(
            sut.informationState.mainWindow,
            sut.dragState.getCurrentReverseDopTarget()?.tabRowKey?.windowState
        )
        assertNull(sut.dragState.getCurrentDropTarget())

        sut.executeAction(DragAndDropAction.StartDragging(editor1))
        sut.executeAction(DragAndDropAction.AddDropTarget(editorTabRowKey1, Rect(0f, 0f, 10f, 10f), false))
        sut.executeAction(DragAndDropAction.AddDropTarget(editorTabRowKey2, Rect(20f, 20f, 30f, 30f), false))
        sut.executeAction(DragAndDropAction.AddDropTarget(explorerTabRowKey, Rect(50f, 50f, 100f, 100f), false))
        assertEquals(4, sut.dragState.dropTargets.size)

        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(25.dp, 25.dp)))
        var dropTarget = sut.dragState.getCurrentDropTarget()
        assertNotNull(dropTarget)
        assertFalse { sut.isValidDropTarget(dropTarget!!, editor2) }

        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(5.dp, 5.dp)))
        dropTarget = sut.dragState.getCurrentDropTarget()
        assertNotNull(dropTarget)
        assertTrue { sut.isValidDropTarget(dropTarget!!, editor2) }

        sut.executeAction(DragAndDropAction.StartDragging(explorer))
        //drop target is no longer valid, position matches bounds
        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(25.dp, 25.dp)))
        dropTarget = sut.dragState.getCurrentDropTarget()
        assertNotNull(dropTarget)
        assertFalse { sut.isValidDropTarget(dropTarget, explorer) }
    }

    @Test
    fun dropDraggedModule() {
        val editorModule = WorkbenchModule(ModuleType.EDITOR,"type", title ={"title"}, loader = {_, _ -> "model"})  {}
        sut.executeAction(WorkbenchActionSync.RegisterEditor("type", editorModule))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 23))
        sut.executeAction(WorkbenchActionSync.RequestEditorState("type", 24))

        //module1 Dragged to Window
        sut.executeAction(DragAndDropAction.StartDragging(sut.informationState.modules[0]))
        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(70.dp,70.dp)))
        sut.executeAction(WorkbenchAction.DropDraggedModule())

        val moduleInWindow = sut.informationState.modules[1]
        assertNotEquals(moduleInWindow.window, sut.informationState.mainWindow)

        sut.executeAction(DragAndDropAction.AddDropTarget(tabRowKey = TabRowKey(DisplayType.WINDOW, ModuleType.BOTH, sut.informationState.mainWindow), bounds = Rect(10f,10f,50f,50f), isReverse = true )) //main
        sut.executeAction(DragAndDropAction.AddDropTarget(tabRowKey = TabRowKey(moduleInWindow), bounds = Rect(60f,60f,100f,100f), isReverse = true )) //window
        sut.executeAction(DragAndDropAction.AddDropTarget(tabRowKey = TabRowKey(DisplayType.TAB1, ModuleType.EDITOR, sut.informationState.mainWindow), bounds = Rect(15f,15f,40f,40f), isReverse = false )) //editor space
        //module in window is dragged back to main
        sut.executeAction(DragAndDropAction.StartDragging(moduleInWindow))
        sut.executeAction(DragAndDropAction.SetPosition(DpOffset(20.dp,20.dp)))
        sut.executeAction(WorkbenchAction.DropDraggedModule())

        assertEquals(sut.informationState.modules[0].window, sut.informationState.modules[1].window)
        assertTrue { sut.dragState.dropTargets.isEmpty() }
    }
}