package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Address
import net.dankito.jpa.entitymanager.IEntityManager


class AddressDao(entityManager: IEntityManager)
    : IAddressDao, CouchbaseBasedDao<Address>(Address::class.java, entityManager)