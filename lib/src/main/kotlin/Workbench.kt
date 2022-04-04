
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.application
import model.WorkbenchModel
import model.data.ModuleType
import model.data.WorkbenchModule
import model.state.DisplayType
import model.state.WorkbenchModuleState
import view.WindowSpace
import view.WorkbenchMainUI

class Workbench {

    private val model: WorkbenchModel = WorkbenchModel()

    fun <M> registerExplorer(
        type: String,
        content: @Composable (M) -> Unit,
    ){
        val explorer = WorkbenchModule<M>(
            moduleType = ModuleType.EXPLORER,
            modelType = type,
            content = content)

        model.registeredExplorers.put(type, explorer);
    }


    fun <M> registerEditor(
        type: String,
        content: @Composable (M) -> Unit
    ){
        val explorer = WorkbenchModule<M>(
            moduleType = ModuleType.EDITOR,
            modelType = type,
            content = content)
        model.registeredEditors.put(type, explorer);
    }


    fun <M> requestExplorer(type: String, title: String, m: M) {
        var explorer = model.registeredExplorers.get(type)
        if(explorer != null){
            explorer as WorkbenchModule<M>
            val state = WorkbenchModuleState(title, m, explorer, model::removeTab, DisplayType.RIGHT)
            model.modules += state
            model.selectedExplorer = state
        }
    }


    fun <M> requestEditor(type: String, title: String, m: M, onClose: (M) -> Unit ={}) {
        var editor = model.registeredEditors.get(type)
        if(editor != null){
            editor as WorkbenchModule<M>
            val t = WorkbenchModuleState<M>(title, m, editor, model::removeTab, DisplayType.TAB, onClose)
            model.modules += t
            model.selectedTab = t
        }
    }

    fun run() = application {
        // init mainwindow
        WorkbenchMainUI(model, ::exitApplication)

        // render seperated windows
        WindowSpace(model)
    }

    internal fun getModel(): WorkbenchModel {
        return model
    }
}
