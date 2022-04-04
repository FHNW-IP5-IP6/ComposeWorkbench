package controller

import Workbench
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ExplorerControllerTest{

    private var workbench = Workbench()
    private var sut = ExplorerController(workbench.getModel())

    @BeforeEach
    fun setup(){
        workbench = Workbench()
        sut = ExplorerController(workbench.getModel())
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.getIndex(null))
    }

    @Test
    fun getIndex_NoExplorers() {
        workbench.registerExplorer<String>(type = "String"){}
        var model = "value"
        workbench.requestExplorer<String>("String", "String Explorer", model)
        val explorer = workbench.getModel().modules.get(0);
        assertEquals(0, sut.getIndex(explorer))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        workbench.registerExplorer<String>(type = "String"){}
        var model = "value"
        workbench.requestExplorer<String>("String", "Explorer 1", model)


        val explorer1 = workbench.getModel().modules.get(0);
        val explorer2 = WorkbenchModuleState<String>(
            "Explorer 2",
            "model",
            WorkbenchModule(ModuleType.EXPLORER, "String"){},
            displayType = DisplayType.RIGHT
        )

        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun getIndex_ExplorerInList() {
        workbench.registerExplorer<String>(type = "String"){}
        var model = "value"
        workbench.requestExplorer<String>("String", "Explorer 1", model)
        workbench.requestExplorer<String>("String", "Explorer 2", model)

        val explorer1 = workbench.getModel().modules.get(0);
        val explorer2 = workbench.getModel().modules.get(1);

        assertEquals(0, sut.getIndex(explorer1))
        assertEquals(1, sut.getIndex(explorer2))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        workbench.registerExplorer<String>(type = "String"){}
        var model = "value"
        workbench.requestExplorer<String>("String", "Explorer 1", model)
        workbench.requestExplorer<String>("String", "Explorer 2", model)

        val explorer1 = workbench.getModel().modules.get(0);
        val explorer2 = workbench.getModel().modules.get(1);

        assertFalse { sut.isExplorerSelected(explorer1) }

        sut.explorerSelectorPressed(explorer1)
        assertTrue { sut.isExplorerSelected(explorer1) }
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        workbench.registerExplorer<String>(type = "String"){}
        var model = "value"
        workbench.requestExplorer<String>("String", "Explorer 1", model)
        workbench.requestExplorer<String>("String", "Explorer 2", model)

        val explorer1 = workbench.getModel().modules.get(0);
        val explorer2 = workbench.getModel().modules.get(1);

        workbench.getModel().selectedExplorer = explorer2

        sut.explorerSelectorPressed(explorer1)
        assertTrue(sut.isExplorerSelected(explorer1))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        workbench.registerExplorer<String>(type = "String"){}
        var model = "value"
        workbench.requestExplorer<String>("String", "Explorer 1", model)

        val explorer1 = workbench.getModel().modules.get(0);

        sut.explorerSelectorPressed(explorer1)
        assertFalse{ sut.isExplorerSelected(explorer1) }
        assertNull(workbench.getModel().selectedExplorer)
    }
}