package realestateexplorer.data

data class ExplorerData(
    val id: Int,
    val type: String = "COMMERCIAL_BUILDING",
    val street: String = "Bahnhofstr.",
    val streetNumber: String = "5",
    val zipCode: Int = 5210,
    val city: String = "Windisch",
)

