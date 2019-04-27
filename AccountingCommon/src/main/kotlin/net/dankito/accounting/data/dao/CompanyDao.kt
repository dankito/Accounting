package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Company
import net.dankito.jpa.entitymanager.IEntityManager


class CompanyDao(entityManager: IEntityManager)
    : ICompanyDao, CouchbaseBasedDao<Company>(Company::class.java, entityManager)