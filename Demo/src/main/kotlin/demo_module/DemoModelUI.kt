package demo_module

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun DemoModuleUI(model: DemoModuleModel, onEdit: () -> Unit) {
    Text(model.name)

    Button(
        onClick = {onEdit.invoke()}
    ) {
        Text("Edit Name")
    }
}