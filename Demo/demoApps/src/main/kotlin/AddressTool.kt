// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.application

fun main() {

    val workbench: Workbench = Workbench()
    val type: WorkbenchEditorType = object :WorkbenchEditorType {
        override fun identifier(): String {
            return "Address"
        }
    }

    workbench.registerExplorer("Address List") {
        AddressExplorerUi(AddressExplorerModel()) {
            workbench.openEditor<Address, AddressEditorModel>(type, it)
        }
    }

    workbench.registerEditor<Address, AddressEditorModel>(
        "Edit Address",
        type,
        { AddressEditorModel(it) },
    ) {
        AddressEditorUi(it)
    }

    application {
        workbench.run( onCloseRequest = ::exitApplication )
    }
}
