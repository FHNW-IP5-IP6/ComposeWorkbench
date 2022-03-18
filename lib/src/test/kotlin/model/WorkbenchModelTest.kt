package model

import model.state.WorkbenchExplorerState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WorkbenchModelTest {

    @Test
    fun getIndexTestNoExplorersNoExplorer() {
        assertEquals(0, WorkbenchModel.getIndex(null))
    }

    @Test
    fun getIndexTestNoExplorers() {
        val explorer1 = WorkbenchExplorerState("") {}
        assertEquals(0, WorkbenchModel.getIndex(explorer1))
    }

    @Test
    fun getIndexOneExplorerNotInList() {
        val explorer1 = WorkbenchExplorerState("") {}
        val explorer2 = WorkbenchExplorerState("") {}
        WorkbenchModel.explorers.add(explorer1)

        assertEquals(0, WorkbenchModel.getIndex(explorer2))
    }

    @Test
    fun getIndexExplorerInList() {
        val explorer1 = WorkbenchExplorerState("") {}
        val explorer2 = WorkbenchExplorerState("") {}
        WorkbenchModel.explorers.add(explorer1)
        WorkbenchModel.explorers.add(explorer2)

        assertEquals(1, WorkbenchModel.getIndex(explorer2))
    }
}