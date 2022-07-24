package realestateeditor.data

import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.logging.Logger
import allpurpose.data.CrudRepository

class RealEstateRepository(private val url : String) : CrudRepository<RealEstateData> {
    private val LOGGER: Logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME)

    override fun create() : Int {
        val start = System.currentTimeMillis()

        val sql = "INSERT INTO REAL_ESTATE (ID, TYPE, STREET, STREET_NUMBER, ZIP_CODE, CITY, YEAR_OF_CONSTRUCTION, MARKET_VALUE, DESCRIPTION) VALUES (null, 'APPARTEMENT_BUILDING', '', '', '', '', '', '', '')"

        val connection = DriverManager.getConnection(url)

        connection.use {
            val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)

            stmt.executeUpdate()

            val keys = stmt.generatedKeys
            keys.next()

            LOGGER.info { """$sql
            |   took: ${System.currentTimeMillis() - start} msec. """.trimMargin() }

            return keys.getInt(1)
        }
    }

    override fun update(data: RealEstateData) {
        val start = System.currentTimeMillis()

        val sql = """UPDATE REAL_ESTATE SET TYPE                 = '${data.type.value}', 
              |                             STREET               = '${data.street.value}', 
              |                             STREET_NUMBER        = '${data.streetNumber.value}', 
              |                             ZIP_CODE             = '${data.zipCode.value}', 
              |                             CITY                 = '${data.city.value}', 
              |                             YEAR_OF_CONSTRUCTION = '${data.yearOfConstruction.value}', 
              |                             MARKET_VALUE         = '${data.marketValue.value}', 
              |                             DESCRIPTION          = '${data.description.value}' 
              |                         where ID = ${data.id}""".trimMargin()

        val connection = DriverManager.getConnection(url)

        connection.use {
            try {
                connection.createStatement().executeUpdate(sql)
            }
            catch (e: SQLException){
                LOGGER.severe{ e.localizedMessage }
            }

        }

        LOGGER.info { """$sql
            |   took: ${System.currentTimeMillis() - start} msec""".trimMargin() }
    }

    override fun read(id: Int) : RealEstateData {
        val start = System.currentTimeMillis()

        val connection = DriverManager.getConnection(url)

        val query = "SELECT * FROM REAL_ESTATE where ID = $id"

        connection.use {
            val rs = connection.createStatement().executeQuery(query)

            rs.next()

            LOGGER.info {
                """$query
            |   took: ${System.currentTimeMillis() - start} msec. """.trimMargin()
            }

            return rs.asRealEstate()
        }
    }

    override fun delete(id: Int) {
        val start = System.currentTimeMillis()

        val sql = "DELETE from REAL_ESTATE where ID = $id"

        val connection = DriverManager.getConnection(url)

        connection.use {
            connection.createStatement().execute(sql)
        }

        LOGGER.info {"""$sql
            |   took: ${System.currentTimeMillis() - start} msec. """.trimMargin() }
    }


    fun readAll() : List<RealEstateData> {
        val start = System.currentTimeMillis()

        val connection = DriverManager.getConnection(url)

        val query = "SELECT * FROM REAL_ESTATE"

        connection.use {
            val rs = connection.createStatement().executeQuery(query)

            val result = mutableListOf<RealEstateData>()
            while(rs.next()){
                result.add(rs.asRealEstate())
            }

            LOGGER.info {
                """$query
            |   took: ${System.currentTimeMillis() - start} msec. """.trimMargin()
            }

            return result
        }
    }

    private fun ResultSet.asRealEstate() : RealEstateData = RealEstateData(id = getInt(1),
                                                                         type = RealEstateType.valueOf(getString(2)),
                                                                       street = getString(3),
                                                                 streetNumber = getString(4),
                                                                      zipCode = getInt(5),
                                                                         city = getString(6),
                                                           yearOfConstruction = getInt(7),
                                                                  marketValue = getInt(8),
                                                                  description = getString(9))
}


/*

CREATE TABLE REAL_ESTATE (
   ID                   INTEGER       PRIMARY KEY AUTOINCREMENT,
   TYPE                 VARCHAR(25)   NOT NULL,
   STREET               VARCHAR(150)  NOT NULL,
   STREET_NUMBER        VARCHAR(10)   NOT NULL,
   ZIP_CODE             INTEGER(5)    NOT NULL,
   CITY                 VARCHAR(200)  NOT NULL,
   YEAR_OF_CONSTRUCTION INTEGER(4)    NOT NULL,
   MARKET_VALUE         INTEGER       NOT NULL,
   DESCRIPTION          VARCHAR(1024) NOT NULL
 );

 */