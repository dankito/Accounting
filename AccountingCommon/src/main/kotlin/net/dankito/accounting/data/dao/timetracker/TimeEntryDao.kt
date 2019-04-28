package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.TimeEntry
import net.dankito.jpa.entitymanager.IEntityManager


class TimeEntryDao(entityManager: IEntityManager)
    : ITimeEntryDao, CouchbaseBasedDao<TimeEntry>(TimeEntry::class.java, entityManager)