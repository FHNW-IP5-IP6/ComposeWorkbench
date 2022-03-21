package model

import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkbenchModelTest {

    private var sut = WorkbenchModel()

    @Test
    fun initialStates() {
        assertTrue{ sut.windows.isEmpty() }
        assertTrue{ sut.explorers.isEmpty() }
        assertTrue{ sut.editors.isEmpty() }
        assertNull(sut.selectedExplorer)
    }
}