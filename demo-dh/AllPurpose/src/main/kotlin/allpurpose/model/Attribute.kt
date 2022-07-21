package allpurpose.model

data class Attribute<T>(
    val value: T,
    val valueAsText: String,
    val isValid: Boolean
)

