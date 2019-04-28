package net.dankito.accounting.data.dao.banking

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.jpa.entitymanager.IEntityManager


class BankAccountDao(entityManager: IEntityManager)
    : IBankAccountDao, CouchbaseBasedDao<BankAccount>(BankAccount::class.java, entityManager)