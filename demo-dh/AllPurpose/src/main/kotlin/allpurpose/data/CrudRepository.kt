package allpurpose.data

interface CrudRepository<D> {
    fun create() : Int
    fun read(id: Int) : D
    fun update(data: D)
    fun delete(id: Int)
}