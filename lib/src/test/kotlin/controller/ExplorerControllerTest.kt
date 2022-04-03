package controller

import model.WorkbenchModel
import model.data.WorkbenchExplorer
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class ExplorerControllerTest{

    private var model = WorkbenchModel()
    private var sut = ExplorerController(model)

    @BeforeEach
    fun setup(){
        model = WorkbenchModel()
        sut = ExplorerController(model)
    }

    @Test
    fun getIndex_NoExplorersNoExplorer() {
        assertEquals(0, sut.getIndex(null))
    }

    @Test
    fun getIndex_NoExplorers() {
        val explorer1 = WorkbenchExplorer("") {}
        assertEquals(0, sut.getIndex(explorer1))
    }

    @Test
    fun getIndex_OneExplorerNotInList() {
        val explorer1 = WorkbenchExplorer("1") {}
        val explorer2 = WorkbenchExplorer("2") {}
        model.explorers.add(explorer1)

        assertEquals(0, sut.getIndex(explorer2))
    }

    @Test
    fun getIndex_ExplorerInList() {
        val explorer1 = WorkbenchExplorer("1") {}
        val explorer2 = WorkbenchExplorer("2") {}
        model.explorers.add(explorer1)
        model.explorers.add(explorer2)

        assertEquals(0, sut.getIndex(explorer1))
        assertEquals(1, sut.getIndex(explorer2))
    }

    @Test
    fun explorerSelectorPresser_NoExplorerSelected(){
        val explorer1 = WorkbenchExplorer("") {}
        assertFalse { sut.isExplorerSelected(explorer1) }

        sut.explorerSelectorPressed(explorer1)
        assertTrue { sut.isExplorerSelected(explorer1) }
    }

    @Test
    fun explorerSelectorPresser_DifferentExplorerSelected(){
        val explorer1 = WorkbenchExplorer("1") {}
        val explorer2 = WorkbenchExplorer("2") {}
        model.selectedExplorer = explorer2

        sut.explorerSelectorPressed(explorer1)
        assertTrue(sut.isExplorerSelected(explorer1))
    }

    @Test
    fun explorerSelectorPresser_ExplorerIsSelected(){
        val explorer1 = WorkbenchExplorer("1") {}
        model.selectedExplorer = explorer1

        sut.explorerSelectorPressed(explorer1)
        assertFalse{ sut.isExplorerSelected(explorer1) }
        assertNull(model.selectedExplorer)
    }
}