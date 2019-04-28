package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.jpa.entitymanager.IEntityManager


class TimeTrackerAccountDao(entityManager: IEntityManager)
    : ITimeTrackerAccountDao, CouchbaseBasedDao<TimeTrackerAccount>(TimeTrackerAccount::class.java, entityManager)