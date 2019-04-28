package net.dankito.accounting.data.dao.banking

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.banking.BankAccountTransactions
import net.dankito.jpa.entitymanager.IEntityManager


class BankAccountTransactionsDao(entityManager: IEntityManager)
    : IBankAccountTransactionsDao, CouchbaseBasedDao<BankAccountTransactions>(BankAccountTransactions::class.java, entityManager)