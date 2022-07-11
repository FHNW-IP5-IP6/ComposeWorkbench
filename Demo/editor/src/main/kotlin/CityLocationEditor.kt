
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.zIndex
import org.jxmapviewer.JXMapViewer
import org.jxmapviewer.OSMTileFactoryInfo
import org.jxmapviewer.input.PanMouseInputListener
import org.jxmapviewer.viewer.DefaultTileFactory
import org.jxmapviewer.viewer.GeoPosition
import org.jxmapviewer.viewer.TileFactoryInfo
import javax.swing.BoxLayout
import javax.swing.JPanel

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        CityMapEditorUi(findById(2661374).toCityLocationState()) {}
    }
}

var map: JXMapViewer? = null

@Composable
fun CityMapEditorUi(model: CityLocationState, onChange: ()->Unit){
    Row {
        SwingPanel(
            modifier = Modifier.weight(1f).fillMaxHeight().zIndex(-99.0f),
            background = Color.Transparent,
            factory = {
                JPanel().apply {
                    map = createMap()
                    map!!.addressLocation = GeoPosition(model.latitude, model.longitude)
                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                    add(map)
                    isVisible = true
                }
            }
        )
        Column(modifier = Modifier.fillMaxHeight(),
        ) {
            IconButton(onClick = { map!!.zoom = map!!.zoom - 1 },
            ){
                Icon(Icons.Filled.Add, "")
            }
            IconButton(onClick = { map!!.zoom = map!!.zoom + 1 },
            ){
                Icon(Icons.Filled.Close, "")
            }
            IconButton(onClick = {
                val center = map!!.centerPosition
                model.longitude = center.longitude
                model.latitude = center.latitude
                onChange.invoke()
            },
            ){
                Icon(Icons.Filled.Refresh, "")
            }
        }
    }
}

fun createMap() : JXMapViewer {
    val mapViewer = JXMapViewer()

    val info: TileFactoryInfo = OSMTileFactoryInfo()
    val tileFactory = DefaultTileFactory(info)
    mapViewer.tileFactory = tileFactory
    mapViewer.zoom = 5

    val listener = PanMouseInputListener(mapViewer)
    mapViewer.addMouseListener(listener)
    mapViewer.addMouseMotionListener(listener)

    return mapViewer
}
