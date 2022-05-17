fun main() {

    val workbench: Workbench = Workbench()

    workbench.registerEditor<CityState>("City", loader = {getCity(it)}) {
            m -> CityEditorUi(m)
    }

    workbench.registerExplorer<CitiesState>("Cities"
    ) { m ->
        CitiesExplorerUi(m) {
            workbench.requestEditor<CityState>("City", "City Editor", it) { mm ->
                mm.persist()
                m.reload()
            }
        }
    }

    workbench.requestExplorer("Cities", "Swiss Cities", getSwissCities(), true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", "Small Cities", getSmallCities(), true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", "Big Cities", getBigCities(), false, ExplorerLocation.BOTTOM)
    workbench.requestExplorer("Cities", "German Cities", getGermanCities(), true, ExplorerLocation.BOTTOM, false)
    workbench.run { println("Exit my Compose Workbench App") }
}
