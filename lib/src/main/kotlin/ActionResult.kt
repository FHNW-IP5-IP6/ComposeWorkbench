data class ActionResult(
    val successful: Boolean,
    val message: String
) {
}

fun success() = ActionResult(true, "")
fun failure(message: String) = ActionResult(false, message)