package allpurpose.model

data class Attribute<T>(
    val dbName: String,
    val value: T,
    val valueAsText: String,
    val isValid: Boolean
)

