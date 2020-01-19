package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.person.Person
import net.dankito.jpa.entitymanager.IEntityManager


class PersonDao(entityManager: IEntityManager)
    : IPersonDao, CouchbaseBasedDao<Person>(Person::class.java, entityManager)