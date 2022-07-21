package realestateeditor.data



import allpurpose.data.Identifiable
import allpurpose.model.Attribute
import allpurpose.view.format

data class RealEstateData(
    override val id: Int,
    val type: Attribute<RealEstateType> = Attribute(RealEstateType.APPARTEMENT_BUILDING, RealEstateType.APPARTEMENT_BUILDING.translation, true),
    val street:             Attribute<String> = Attribute("", "", true),
    val streetNumber:       Attribute<String> = Attribute("", "", true),
    val zipCode:            Attribute<Int>    = Attribute(5210, "5210", true),
    val city:               Attribute<String> = Attribute("Windisch", "Windisch", true),
    val yearOfConstruction: Attribute<Int>    = Attribute(2014, "2014", true),
    val marketValue:        Attribute<Int>    = Attribute(1_000_000, 1_000_000.format("%,d"), true), //todo: no dependency to view
    val description:        Attribute<String> = Attribute("", "", true)
) : Identifiable{
    constructor(id: Int, type: RealEstateType, street: String, streetNumber: String, zipCode: Int, city: String, yearOfConstruction: Int, marketValue: Int, description: String) :
        this(id = id,
            type = Attribute(type,type.translation, true),
            street = Attribute(street, street, true),
            streetNumber = Attribute(streetNumber, streetNumber, true),
            zipCode = Attribute(zipCode, zipCode.toString(), true),
            city = Attribute(city, city, true),
            yearOfConstruction = Attribute(yearOfConstruction, yearOfConstruction.toString(), true),
            marketValue = Attribute(marketValue, marketValue.format("%,d"), true),
            description = Attribute(description, description, true)
        )

}
