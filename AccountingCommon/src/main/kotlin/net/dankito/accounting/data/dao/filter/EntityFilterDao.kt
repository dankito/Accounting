package net.dankito.accounting.data.dao.filter

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.filter.EntityFilter
import net.dankito.jpa.entitymanager.IEntityManager


class EntityFilterDao(entityManager: IEntityManager)
    : IEntityFilterDao, CouchbaseBasedDao<EntityFilter>(EntityFilter::class.java, entityManager)