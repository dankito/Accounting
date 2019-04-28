package net.dankito.accounting.data.dao.banking

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.jpa.entitymanager.IEntityManager


class BankAccountTransactionDao(entityManager: IEntityManager)
    : IBankAccountTransactionDao, CouchbaseBasedDao<BankAccountTransaction>(BankAccountTransaction::class.java, entityManager)