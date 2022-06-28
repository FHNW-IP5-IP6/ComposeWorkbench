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

class WorkbenchDisplayControllerTest {

    private val displayType = DisplayType.LEFT
    private val moduleType = ModuleType.EXPLORER

    private var workbench = Workbench()
    private var sut = workbench.getWorkbenchController().createModuleDisplayController(displayType, moduleType, true)
    private var controller = workbench.getWorkbenchController()

    @BeforeEach
    fun setup(){
        workbench = Workbench()
        sut = workbench.getWorkbenchController().createModuleDisplayController(displayType, moduleType, true)
        controller = workbench.getWorkbenchController()
    }

    @Test
    fun removeModule_NoModules() {
        val module = WorkbenchModule<String>(moduleType,"type", title ={"title"}) {}
        val moduleState = WorkbenchModuleState<String>(id = 1, model = "model", module = module, displayType = displayType)
        sut.removeModuleState(moduleState)

        assertEquals(0, sut.getModulesFiltered().size)
    }

    @Test
    fun removeModule_RemoveModule() {
        val module = WorkbenchModule<String>(moduleType,"type", title ={"title"}) {}
        val moduleState = WorkbenchModuleState(id = 1, model = "model", module = module, displayType = displayType)
        controller.selectionController.addModuleState(moduleState)
        assertTrue { sut.getModulesFiltered().contains(moduleState) }

        sut.removeModuleState(moduleState)
        assertEquals(0, sut.getModulesFiltered().size)
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.getIndex(null))
    }

    @Test
    fun getIndex_NoExplorers() {
        workbench.registerExplorer<String>(type = "String", title = {"title"}){}
        val model = "value"
        workbench.requestExplorer("String", model)
        val explorer = sut.getSelectedModule()
        assertEquals(0, sut.getIndex(explorer))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        workbench.registerExplorer<String>(type = "String", title = {"title"}){}
        val model = "value"
        workbench.requestExplorer("String", model)

        val explorer2 = WorkbenchModuleState(
            id = 1,
            model = "model",
            module = WorkbenchModule(moduleType = moduleType, modelType = "String", title = {"title"}){},
            displayType = displayType
        )

        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun getIndex_ExplorerInList() {
        workbench.registerExplorer<String>(type = "String", title = {"title"}){}
        workbench.requestExplorer("String", "model1")
        workbench.requestExplorer("String", "model2")

        val explorer1 = sut.getModulesFiltered()[0]
        val explorer2 = sut.getModulesFiltered()[1]

        assertEquals(0, sut.getIndex(explorer1))
        assertEquals(1, sut.getIndex(explorer2))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        workbench.registerExplorer<String>(type = "String", title = {"title"}){}
        workbench.requestExplorer("String","model1")
        workbench.requestExplorer("String","model2")

        val explorer1 = sut.getModulesFiltered()[1]
        assertFalse { sut.isModuleSelected(explorer1) }

        sut.moduleSelectorPressed(explorer1)
        assertTrue { sut.isModuleSelected(explorer1) }
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        workbench.registerExplorer<String>(type = "String", title = {"title"}){}
        workbench.requestExplorer("String","model1")
        workbench.requestExplorer("String","model2")

        val explorer1 = sut.getModulesFiltered()[1]
        val explorer2 = sut.getModulesFiltered()[0]

        controller.selectionController.setSelectedModule(explorer2)

        sut.moduleSelectorPressed(explorer1)
        assertTrue(sut.isModuleSelected(explorer1))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        workbench.registerExplorer<String>(type = "String", title = {"title"}){}
        val model = "value"
        workbench.requestExplorer("String", model)

        val explorer1 = sut.getModulesFiltered()[0]

        sut.moduleSelectorPressed(explorer1)
        assertFalse{ sut.isModuleSelected(explorer1) }
        assertNull(sut.getSelectedModule())
    }

}