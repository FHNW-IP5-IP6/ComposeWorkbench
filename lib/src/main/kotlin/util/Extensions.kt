package util

import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout

//https://stackoverflow.com/questions/70057396/how-to-show-vertical-text-with-proper-size-layout-in-jetpack-compose
internal fun Modifier.vertical() =
    this.then(
        layout() { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.height, placeable.width) {         //switch with and height
                placeable.place(                                //place correctly
                    x = -(placeable.width / 2 - placeable.height / 2),
                    y = -(placeable.height / 2 - placeable.width / 2)
                )
            }
        }
    ).then(this.rotate(-90f))

@Composable
internal fun ButtonDefaults.selectedButtonColors(selected: Boolean) =
    if (selected) {
        ButtonDefaults.outlinedButtonColors(backgroundColor = Color.LightGray) //use color definition fiel
    } else {
        ButtonDefaults.outlinedButtonColors()
    }