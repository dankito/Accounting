package net.dankito.accounting.data.dao.filter

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.filter.Filter
import net.dankito.jpa.entitymanager.IEntityManager


class FilterDao(entityManager: IEntityManager)
    : IFilterDao, CouchbaseBasedDao<Filter>(Filter::class.java, entityManager)