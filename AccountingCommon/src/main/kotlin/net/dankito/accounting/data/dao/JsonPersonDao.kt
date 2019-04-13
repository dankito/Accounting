package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Person
import java.io.File


// TODO: replace by a database based implementation
class JsonPersonDao(dataFolder: File) : IPersonDao, JsonBasedDao<Person>(Person::class.java, dataFolder, "persons.json")