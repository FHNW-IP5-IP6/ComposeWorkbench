// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

fun main() {

    val workbench: Workbench = Workbench()

    workbench.registerEditor<AddressEditorModel>("AddressEditor", loader = {AddressEditorModel(AddressExplorerModel().addressList[it])}) {
            m -> AddressEditorUi(m)
    }

    workbench.registerExplorer<AddressExplorerModel>("AddressExplorer"
    ) { m ->
        AddressExplorerUi(m) {
            workbench.requestEditor<AddressEditorModel>("AddressEditor", "Address Editor", it.id) { mm ->
                m.updateAddress(Address(mm.id, mm.firstName, mm.lastName, mm.street, mm.streetNr, mm.city, mm.country))
            }
        }
    }

    workbench.requestExplorer("AddressExplorer", "Address Explorer", AddressExplorerModel())
    workbench.requestExplorer("AddressExplorer", "Address Explorer2", AddressExplorerModel())
    workbench.requestExplorer("AddressExplorer", "Address Explorer3", AddressExplorerModel())

    workbench.run { println("Exit my Compose Workbench App") }
}
