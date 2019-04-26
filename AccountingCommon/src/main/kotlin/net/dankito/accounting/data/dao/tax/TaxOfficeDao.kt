package net.dankito.accounting.data.dao.tax

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.tax.TaxOffice
import net.dankito.jpa.entitymanager.IEntityManager


class TaxOfficeDao(entityManager: IEntityManager)
    : ITaxOfficeDao, CouchbaseBasedDao<TaxOffice>(TaxOffice::class.java, entityManager)