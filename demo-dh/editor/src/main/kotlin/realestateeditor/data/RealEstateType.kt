package realestateeditor.data

import allpurpose.data.Translatable

enum class RealEstateType(override val translation: String) : Translatable {
    FAMILY_HOUSE("Einfamilienhaus"),
    APPARTEMENT_BUILDING("Mehrfamilienhaus"),
    COMMERCIAL_BUILDING("Gewerblich")
}