package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.TrackedTimes
import net.dankito.jpa.entitymanager.IEntityManager


class TrackedTimesDao(entityManager: IEntityManager)
    : ITrackedTimesDao, CouchbaseBasedDao<TrackedTimes>(TrackedTimes::class.java, entityManager)