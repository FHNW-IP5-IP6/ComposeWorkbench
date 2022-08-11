package view.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
internal fun WorkbenchStateInfo( information: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource("logo/workbench-logo-text-90dpi.png"), "Logo", modifier = Modifier.height(70.dp))
                Spacer(modifier = Modifier.height(40.dp))
                Text(information, fontSize = MaterialTheme.typography.body1.fontSize)
                Spacer(modifier = Modifier.height(20.dp))
                DotsPulsing()
            }
    }
}