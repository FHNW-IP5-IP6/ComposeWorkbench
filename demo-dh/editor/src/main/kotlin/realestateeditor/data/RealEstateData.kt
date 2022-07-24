package realestateeditor.data



import allpurpose.data.Identifiable
import allpurpose.model.Attribute
import allpurpose.view.format

data class RealEstateData(
    override val id: Int,
    val type: Attribute<RealEstateType> = Attribute("TYPE", RealEstateType.APPARTEMENT_BUILDING, RealEstateType.APPARTEMENT_BUILDING.translation, true),
    val street:             Attribute<String> = Attribute("STREET", "", "", true),
    val streetNumber:       Attribute<String> = Attribute("STREET_NUMBER", "", "", true),
    val zipCode:            Attribute<Int>    = Attribute("ZIP_CODE", 5210, "5210", true),
    val city:               Attribute<String> = Attribute("CITY", "Windisch", "Windisch", true),
    val yearOfConstruction: Attribute<Int>    = Attribute("YEAR_OF_CONSTRUCTION", 2014, "2014", true),
    val marketValue:        Attribute<Int>    = Attribute("MARKET_VALUE", 1_000_000, 1_000_000.format("%,d"), true), //todo: no dependency to view
    val description:        Attribute<String> = Attribute("DESCRIPTION", "", "", true)
) : Identifiable{
    constructor(id: Int, type: RealEstateType, street: String, streetNumber: String, zipCode: Int, city: String, yearOfConstruction: Int, marketValue: Int, description: String) :
        this(              id = id,
                        type = Attribute("TYPE", type,type.translation, true),
                      street = Attribute("STREET", street, street, true),
                streetNumber = Attribute("STREET_NUMBER", streetNumber, streetNumber, true),
                     zipCode = Attribute("ZIP_CODE", zipCode, zipCode.toString(), true),
                        city = Attribute("CITY", city, city, true),
          yearOfConstruction = Attribute("YEAR_OF_CONSTRUCTION", yearOfConstruction, yearOfConstruction.toString(), true),
                 marketValue = Attribute("MARKET_VALUE", marketValue, marketValue.format("%,d"), true),
                 description = Attribute("DESCRIPTION", description, description, true)
        )

}
