package demo_module

import androidx.compose.runtime.Composable

class DemoModule {

    val model : DemoModuleModel = DemoModuleModel()
    var onEdit : () -> Unit = {println("DemoModule: edit button pressed")}

    fun mainView() : @Composable () -> Unit {
        return {DemoModuleUI(model, onEdit)}
    }




}