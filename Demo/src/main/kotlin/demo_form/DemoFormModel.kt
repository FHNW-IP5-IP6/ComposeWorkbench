package demo_form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.internal.StabilityInferred
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DemoFormModel (name: String) {
    var name  by mutableStateOf(name)
}