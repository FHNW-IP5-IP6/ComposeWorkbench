package realestateexplorer.data

import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement

class Repository(private val url : String) {
    fun readAll(): List<ExplorerData> {

        val connection = DriverManager.getConnection(url)

        val query = "SELECT ID, TYPE, STREET, STREET_NUMBER, ZIP_CODE, CITY FROM REAL_ESTATE"

        connection.use {
            val rs = connection.createStatement().executeQuery(query)

            val result = mutableListOf<ExplorerData>()
            while (rs.next()) {
                result.add(rs.asRealEstate())
            }


            return result
        }
    }

    private fun ResultSet.asRealEstate(): ExplorerData = ExplorerData(
        id = getInt(1),
        type = getString(2),
        street = getString(3),
        streetNumber = getString(4),
        zipCode = getInt(5),
        city = getString(6)
    )

    fun create() : Int {
        val sql = "INSERT INTO REAL_ESTATE (ID, TYPE, STREET, STREET_NUMBER, ZIP_CODE, CITY, YEAR_OF_CONSTRUCTION, MARKET_VALUE, DESCRIPTION) VALUES (null, 'APPARTEMENT_BUILDING', '', '', '', '', '', '', '')"

        val connection = DriverManager.getConnection(url)

        connection.use {
            val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            stmt.executeUpdate()

            val keys = stmt.generatedKeys
            keys.next()

            return keys.getInt(1)
        }
    }
}
