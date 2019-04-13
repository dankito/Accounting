package net.dankito.accounting.service.person

import net.dankito.accounting.data.model.Person


interface IPersonService {

    fun getAll(): List<Person>

    fun saveOrUpdate(person: Person)

    fun delete(person: Person)

}