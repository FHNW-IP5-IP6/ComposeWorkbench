package realestateeditor.data

data class ApplicationState(val isDialogOpen: Boolean = false, val realEstates: List<RealEstateData>)