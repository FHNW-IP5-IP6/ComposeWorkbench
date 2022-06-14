package controller

import Workbench
import model.data.DisplayType
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.WorkbenchModuleState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkbenchModuleControllerTest {

    private val displayType = DisplayType.LEFT
    private val moduleType = ModuleType.EXPLORER

    private var workbench = Workbench()
    private var sut = WorkbenchModuleController(workbench.getModel(), displayType, moduleType, true)


    @BeforeEach
    fun setup(){
        workbench = Workbench()
        sut = WorkbenchModuleController(workbench.getModel(), displayType, moduleType, true)
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.getIndex(null))
    }

    @Test
    fun getIndex_NoExplorers() {
        workbench.registerExplorer<String>(type = "String"){}
        val model = "value"
        workbench.requestExplorer<String>("String", {"title"}, model)
        val explorer = workbench.getModel().modules[0]
        assertEquals(0, sut.getIndex(explorer))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        workbench.registerExplorer<String>(type = "String"){}
        val model = "value"
        workbench.requestExplorer<String>("String", {"title"}, model)

        val explorer2 = WorkbenchModuleState<String>(
            id = 1,
            title ={"title 2"},
            model = "model",
            module = WorkbenchModule(moduleType, "String"){},
            displayType = displayType
        )

        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun getIndex_ExplorerInList() {
        workbench.registerExplorer<String>(type = "String"){}
        workbench.requestExplorer<String>("String", {"title"}, "model1")
        workbench.requestExplorer<String>("String", {"title 2"}, "model2")

        val explorer1 = workbench.getModel().modules[0]
        val explorer2 = workbench.getModel().modules[1]

        assertEquals(1, sut.getIndex(explorer1))
        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        workbench.registerExplorer<String>(type = "String"){}
        workbench.requestExplorer<String>("String", {"title"}, "model1")
        workbench.requestExplorer<String>("String", {"title 2"}, "model2")

        val explorer1 = workbench.getModel().modules[0]

        assertFalse { sut.isModuleSelected(explorer1) }

        sut.moduleSelectorPressed(explorer1)
        assertTrue { sut.isModuleSelected(explorer1) }
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        workbench.registerExplorer<String>(type = "String"){}
        workbench.requestExplorer<String>("String", {"title"}, "model1")
        workbench.requestExplorer<String>("String", {"title 2"}, "model2")

        val explorer1 = workbench.getModel().modules[0]
        val explorer2 = workbench.getModel().modules[1]

        workbench.getModel().setSelectedModule(explorer2)

        sut.moduleSelectorPressed(explorer1)
        assertTrue(sut.isModuleSelected(explorer1))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        workbench.registerExplorer<String>(type = "String"){}
        val model = "value"
        workbench.requestExplorer<String>("String", {"title"}, model)

        val explorer1 = workbench.getModel().modules[0]

        sut.moduleSelectorPressed(explorer1)
        assertFalse{ sut.isModuleSelected(explorer1) }
        assertNull(sut.getSelectedModule())
    }

}