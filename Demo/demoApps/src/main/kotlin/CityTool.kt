fun main() {

    val workbench: Workbench = Workbench()

    workbench.registerEditor<CityState>("City", loader = {getCity(it)}) {
            m -> CityEditorUi(m)
    }

    workbench.registerExplorer<CitiesState>("Cities"
    ) { m ->
        CitiesExplorerUi(m) {
            workbench.requestEditor<CityState>("City", {m -> m.name}, it) { mm ->
                mm.persist()
                m.reload()
            }
        }
    }

    workbench.requestExplorer("Cities", {m -> "Swiss Cities: ${m.state.size}"}, getSwissCities(), true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", {m -> "Small Cities: ${m.state.size}"}, getSmallCities(), true, ExplorerLocation.LEFT)
    workbench.requestExplorer("Cities", {m -> "Big Cities: ${m.state.size}"}, getBigCities(), false, ExplorerLocation.BOTTOM)
    workbench.requestExplorer("Cities", {m -> "German Cities: ${m.state.size}"}, getGermanCities(), true, ExplorerLocation.BOTTOM, false)
    workbench.run { println("Exit my Compose Workbench App") }
}
