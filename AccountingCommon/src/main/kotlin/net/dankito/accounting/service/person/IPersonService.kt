package net.dankito.accounting.service.person

import net.dankito.accounting.data.model.person.Company
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.Person
import net.dankito.accounting.data.model.person.PersonType


interface IPersonService {

    fun getAll(): List<NaturalOrLegalPerson>

    fun getAllOfType(type: PersonType): List<NaturalOrLegalPerson>

    fun getAllPersons(): List<Person>

    fun getAllCompanies(): List<Company>


    fun saveOrUpdate(person: NaturalOrLegalPerson)

    fun delete(person: NaturalOrLegalPerson)

}