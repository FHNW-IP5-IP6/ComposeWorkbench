import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun EditableField(label:String ,getValue: () -> String, onValueChange: (String) -> Unit){
    TextField(
        modifier = Modifier.padding(start = 5.dp, end = 5.dp, bottom = 5.dp),
        label = { Text(text = label) },
        value = getValue.invoke(),
        onValueChange =  onValueChange
    )
}