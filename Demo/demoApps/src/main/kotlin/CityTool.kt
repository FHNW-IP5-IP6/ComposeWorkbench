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

    workbench.registerDefaultExplorer("Cities", "Swiss Cities") {
        getSwissCities()
    }
    workbench.registerDefaultExplorer("Cities", "Small Cities") {
        getSmallCities()
    }

    workbench.requestExplorer("Cities", "Swiss Cities", getBigCities(), ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", "Small Cities", getBigCities(), ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", "Big Cities", getBigCities(), ExplorerLocation.BOTTOM)
    workbench.requestExplorer("Cities", "German Cities", getGermanCities(), ExplorerLocation.BOTTOM)
    workbench.run { println("Exit my Compose Workbench App") }
}
