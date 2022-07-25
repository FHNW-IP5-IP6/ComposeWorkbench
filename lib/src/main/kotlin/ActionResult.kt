data class ActionResult(
    val successful: Boolean,
    val message: String
) {
}

val success = ActionResult(true, "")