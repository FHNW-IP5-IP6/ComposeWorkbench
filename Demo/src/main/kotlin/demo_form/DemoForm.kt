package demo_form

import androidx.compose.runtime.Composable

class DemoForm(name: String) {

    private val demoFormModel : DemoFormModel = DemoFormModel(name)

    var onSave : () -> Unit = {}

    fun getData() : String {
        return demoFormModel.name
    }

    fun mainView() : @Composable () -> Unit {
        return {DemoModuleUI(demoFormModel, onSave)}
    }
}