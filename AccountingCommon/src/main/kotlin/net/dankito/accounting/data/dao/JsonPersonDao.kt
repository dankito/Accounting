package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Person
import java.io.File
import java.util.*


// TODO: replace by a database based implementation
class JsonPersonDao(dataFolder: File) : IPersonDao, JsonBasedDao<Person>(Person::class.java, dataFolder, "persons.json") {

    override fun saveOrUpdate(entity: Person) {
        setIdIfNotPersistedYet(entity.primaryAddress)

        super.saveOrUpdate(entity)
    }
}