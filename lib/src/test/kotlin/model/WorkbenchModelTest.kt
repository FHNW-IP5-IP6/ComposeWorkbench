package model

import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class WorkbenchModelTest {

    private var sut = WorkbenchModel()

    @Test
    fun initialStates() {
        assertTrue{ sut.modules.isEmpty() }
        assertTrue{ sut.registeredExplorers.isEmpty() }
        assertTrue{ sut.registeredEditors.isEmpty() }
        assertNull(sut.selectedExplorer)
    }
}