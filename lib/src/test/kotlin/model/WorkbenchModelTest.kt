package model

import model.state.WorkbenchStaticState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class WorkbenchModelTest {

    private val sut = WorkbenchStaticState()

    @Test
    fun initialStates() {
        assertEquals( 0, sut.registeredEditors.size )
        assertEquals( 0, sut.registeredExplorers.size )
        assertTrue{ sut.registeredExplorers.isEmpty() }
        assertTrue{ sut.registeredEditors.isEmpty() }
    }

}