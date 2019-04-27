package net.dankito.accounting.service.person

import net.dankito.accounting.data.dao.IAddressDao
import net.dankito.accounting.data.dao.ICompanyDao
import net.dankito.accounting.data.dao.IPersonDao
import net.dankito.accounting.data.model.Company
import net.dankito.accounting.data.model.NaturalOrLegalPerson
import net.dankito.accounting.data.model.Person


open class PersonService(protected val personDao: IPersonDao, protected val companyDao: ICompanyDao,
                         protected val addressDao: IAddressDao): IPersonService {

    override fun getAll(): List<NaturalOrLegalPerson> {
        val allPersons = mutableListOf<NaturalOrLegalPerson>()

        allPersons.addAll(getAllPersons())

        allPersons.addAll(getAllCompanies())

        return allPersons
    }

    override fun getAllPersons(): List<Person> {
        return personDao.getAll()
    }

    override fun getAllCompanies(): List<Company> {
        return companyDao.getAll()
    }


    override fun saveOrUpdate(person: NaturalOrLegalPerson) {
        addressDao.saveOrUpdate(person.address)

        if (person is Person) {
            saveOrUpdate(person)
        }
        else if (person is Company) {
            saveOrUpdate(person)
        }
    }

    protected open fun saveOrUpdate(person: Person) {
        personDao.saveOrUpdate(person)
    }

    protected open fun saveOrUpdate(company: Company) {
        companyDao.saveOrUpdate(company)
    }


    override fun delete(person: NaturalOrLegalPerson) {
        if (person is Person) {
            delete(person)
        }
        else if (person is Company) {
            delete(person)
        }

        addressDao.delete(person.address)
    }

    protected open fun delete(person: Person) {
        personDao.delete(person)
    }

    protected open fun delete(company: Company) {
        companyDao.delete(company)
    }

}