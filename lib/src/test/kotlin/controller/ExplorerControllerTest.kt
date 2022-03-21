package controller

import model.WorkbenchModel
import model.state.WorkbenchExplorerState
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ExplorerControllerTest{

    private var model = WorkbenchModel()
    private var sut = ExplorerController(model)

    @BeforeEach
    fun setup(){
        model = WorkbenchModel()
        sut = ExplorerController(model)
    }

    @Test
    fun getIndexTestNoExplorersNoExplorer() {
        assertEquals(0, sut.getIndex(null))
    }

    @Test
    fun getIndexTestNoExplorers() {
        val explorer1 = WorkbenchExplorerState("") {}
        assertEquals(0, sut.getIndex(explorer1))
    }

    @Test
    fun getIndexOneExplorerNotInList() {
        val explorer1 = WorkbenchExplorerState("") {}
        val explorer2 = WorkbenchExplorerState("") {}
        model.explorers.add(explorer1)

        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun getIndexExplorerInList() {
        val explorer1 = WorkbenchExplorerState("") {}
        val explorer2 = WorkbenchExplorerState("") {}
        model.explorers.add(explorer1)
        model.explorers.add(explorer2)

        assertEquals(0, sut.getIndex(explorer1))
        assertEquals(1, sut.getIndex(explorer2))
    }
}