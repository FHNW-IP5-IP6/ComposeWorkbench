// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

fun main() {

    val workbench: Workbench = Workbench()

    workbench.registerEditor<AddressEditorModel>("AddressEditor") {
            m -> AddressEditorUi(m)
    }

    workbench.registerExplorer<AddressExplorerModel>("AddressExplorer"
    ) { m ->
        AddressExplorerUi(m) {
            workbench.requestEditor("AddressEditor", "Address Editor", AddressEditorModel(it), onClose = {mm ->
                m.addressList[m.addressList.indexOf(it)] = Address(mm.firstName, mm.lastName, mm.street, mm.streetNr, mm.city, mm.country)
            })
        }
    }

    workbench.requestExplorer<AddressExplorerModel>("AddressExplorer", "Address Explorer", AddressExplorerModel())

    workbench.run { println("Exit my Compose Workbench App") }
}
