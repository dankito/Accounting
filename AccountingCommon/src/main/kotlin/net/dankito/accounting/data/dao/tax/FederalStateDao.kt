package net.dankito.accounting.data.dao.tax

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.tax.FederalState
import net.dankito.jpa.entitymanager.IEntityManager


class FederalStateDao(entityManager: IEntityManager)
    : IFederalStateDao, CouchbaseBasedDao<FederalState>(FederalState::class.java, entityManager)