package model

import model.state.getDefaultWorkbenchDisplayInformation
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class WorkbenchInformationStateTest {

    private val sut = getDefaultWorkbenchDisplayInformation()

    @Test
    fun initialStates() {
        assertEquals( 0, sut.registeredEditors.size )
        assertEquals( 0, sut.registeredExplorers.size )
        assertTrue{ sut.registeredExplorers.isEmpty() }
        assertTrue{ sut.registeredEditors.isEmpty() }
    }

}