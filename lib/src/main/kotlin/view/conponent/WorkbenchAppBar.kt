package view.conponent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.WorkbenchModel
import model.data.ModuleType
import util.selectedButtonColors

@Composable
internal fun WorkbenchAppBar(model: WorkbenchModel) {
    TopAppBar(content = {
        Row(
            modifier = Modifier.fillMaxSize().padding(20.dp, 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(model.appTitle,
                fontSize = 30.sp,
                color = Color.White,
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
            )
            Button(
                onClick = { model.saveAll(ModuleType.EDITOR) },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                colors = ButtonDefaults.selectedButtonColors(true)
            ) { Text("Save All") }
        }
    })
}