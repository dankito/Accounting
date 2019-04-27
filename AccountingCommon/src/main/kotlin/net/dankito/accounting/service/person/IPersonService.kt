package net.dankito.accounting.service.person

import net.dankito.accounting.data.model.Company
import net.dankito.accounting.data.model.NaturalOrLegalPerson
import net.dankito.accounting.data.model.Person


interface IPersonService {

    fun getAll(): List<NaturalOrLegalPerson>

    fun getAllPersons(): List<Person>

    fun getAllCompanies(): List<Company>


    fun saveOrUpdate(person: NaturalOrLegalPerson)

    fun delete(person: NaturalOrLegalPerson)

}