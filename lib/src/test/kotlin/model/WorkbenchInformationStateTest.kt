package model

import model.state.getDefaultWorkbenchDisplayInformation
import model.state.getMainWorkbenchWindowState
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

    @Test
    fun windowStateCopy() {
        val window = getMainWorkbenchWindowState()
        var windows = sut.windows.toMutableList()
        windows += window
        val newState = sut.copy(windows = windows)
        val stateCopy2 = newState.copy(appTitle = "bla")
        assertTrue { window == stateCopy2.windows[0] }
    }

}