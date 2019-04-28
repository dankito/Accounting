package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.TrackedMonth
import net.dankito.jpa.entitymanager.IEntityManager


class TrackedMonthDao(entityManager: IEntityManager)
    : ITrackedMonthDao, CouchbaseBasedDao<TrackedMonth>(TrackedMonth::class.java, entityManager)