// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.application

fun main() {

    val type: WorkbenchEditorType = object :WorkbenchEditorType {
        override fun identifier(): String {
            return "Address"
        }
    }

    Workbench.registerExplorer("Address List") {
        AddressExplorerUi(AddressExplorerModel()) {
            Workbench.openEditor<Address, AddressEditorModel>(type, it)
        }
    }

    Workbench.registerEditor<Address, AddressEditorModel>(
        "Edit Address",
        type,
        { AddressEditorModel(it) },
    ) {
        AddressEditorUi(it)
    }

    application {
        Workbench.run( onCloseRequest = ::exitApplication )
    }
}
