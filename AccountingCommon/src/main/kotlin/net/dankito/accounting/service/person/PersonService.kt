package net.dankito.accounting.service.person

import net.dankito.accounting.data.dao.IPersonDao
import net.dankito.accounting.data.model.Person


open class PersonService(protected val dao: IPersonDao): IPersonService {

    override fun getAll(): List<Person> {
        return dao.getAll()
    }

    override fun saveOrUpdate(person: Person) {
        dao.saveOrUpdate(person)
    }

    override fun delete(person: Person) {
        dao.delete(person)
    }

}