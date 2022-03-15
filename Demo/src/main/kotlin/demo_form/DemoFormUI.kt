package demo_form

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable

@Composable
fun DemoModuleUI(model: DemoFormModel, onSave: () -> Unit) {
    TextField(
        value = model.name,
        onValueChange = { model.name = it },
        label = { Text("Name") }
    )

    Button(
        onClick = {onSave.invoke()}
    ) {
        Text("Save")
    }
}