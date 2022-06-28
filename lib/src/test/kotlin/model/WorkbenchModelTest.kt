package model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class WorkbenchModelTest {

    private val sut = WorkbenchModel()

    @Test
    fun initialStates() {
        assertEquals( 0, sut.modules.size )
        assertTrue{ sut.registeredExplorers.isEmpty() }
        assertTrue{ sut.registeredEditors.isEmpty() }
    }

}