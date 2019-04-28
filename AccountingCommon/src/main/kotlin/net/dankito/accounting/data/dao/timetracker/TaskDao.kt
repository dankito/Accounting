package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.Task
import net.dankito.jpa.entitymanager.IEntityManager


class TaskDao(entityManager: IEntityManager)
    : ITaskDao, CouchbaseBasedDao<Task>(Task::class.java, entityManager)