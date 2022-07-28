
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

object DbSettings {
    val citiesDb by lazy {
        val path = DbSettings.javaClass.getResource("/db/cities.db").toExternalForm()
        val url = "jdbc:sqlite:$path"
        val driver = "org.sqlite.JDBC"
        val db = Database.connect(url = url, driver = driver)
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        db
    }
}