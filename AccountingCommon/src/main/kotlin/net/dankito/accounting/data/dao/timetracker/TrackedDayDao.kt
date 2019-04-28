package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.TrackedDay
import net.dankito.jpa.entitymanager.IEntityManager


class TrackedDayDao(entityManager: IEntityManager)
    : ITrackedDayDao, CouchbaseBasedDao<TrackedDay>(TrackedDay::class.java, entityManager)