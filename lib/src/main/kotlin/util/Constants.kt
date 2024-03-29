import androidx.compose.ui.unit.dp

internal const val TAB_ROW_HEIGHT = 45f
internal const val TAB_ROW_WIDTH = 60f
internal val ICON_ROW_HEIGHT = 25.dp

internal val MAIN_WINDOW_POS_OFFSET = 50.dp

internal val SPLIT_PAIN_HANDLE_SIZE = 2.dp
internal val SPLIT_PAIN_HANDLE_AREA = 9.dp
internal const val SPLIT_PAIN_HANDLE_ALPHA = 0.50f

internal const val MQ_INTERNAL_BROKER_IP_ADDRESS = "0.0.0.0"
internal const val MQ_INTERNAL_BROKER_PORT = 1883
private const val MQ_INTERNAL_PREFIX = "workbench"
private const val MQ_INTERNAL_TOPIC_EDITOR = "editors"
internal const val MQ_INTERNAL_TOPIC_PATH_EDITOR = "$MQ_INTERNAL_PREFIX/$MQ_INTERNAL_TOPIC_EDITOR"

internal const val MQ_INTERNAL_EDITOR_STATE_CREATED = "created"
internal const val MQ_INTERNAL_EDITOR_STATE_SAVED = "saved"
internal const val MQ_INTERNAL_EDITOR_STATE_UNSAVED = "unsaved"
internal const val MQ_INTERNAL_EDITOR_STATE_CLOSED = "closed"
internal const val MQ_INTERNAL_EDITOR_STATE_SELECTED = "selected"

internal fun toUpdateType(msg: String): UpdateType {
    return when (msg) {
        MQ_INTERNAL_EDITOR_STATE_CREATED -> UpdateType.CREATED
        MQ_INTERNAL_EDITOR_STATE_SAVED -> UpdateType.SAVED
        MQ_INTERNAL_EDITOR_STATE_UNSAVED -> UpdateType.UNSAVED
        MQ_INTERNAL_EDITOR_STATE_CLOSED -> UpdateType.CLOSED
        MQ_INTERNAL_EDITOR_STATE_SELECTED -> UpdateType.SELECTED
        else -> UpdateType.OTHER
    }
}