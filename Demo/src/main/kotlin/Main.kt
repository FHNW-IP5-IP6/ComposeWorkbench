// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import demo_form.DemoForm
import demo_module.DemoModule

import workbench_compose.WorkbenchCompose
import java.lang.Thread.sleep
import kotlin.concurrent.thread

fun main() {
    var wbc :WorkbenchCompose = WorkbenchCompose()

    var demoModule : DemoModule = DemoModule()

    demoModule.onEdit = {
        val form : DemoForm = DemoForm(demoModule.model.name)
        val tab = wbc.addTab("DemoForm from DemoModule", form.mainView())
        form.onSave = {
            demoModule.model.name = form.getData()
            tab.onClose = {
                println("closing FormModule")
            }
            wbc.closeTab(tab)
        }
    }

    thread(name = "Tab Creator") {
        sleep(1000)
        wbc.addTab("DemoModule", demoModule.mainView())
    }

    wbc.run()
}
