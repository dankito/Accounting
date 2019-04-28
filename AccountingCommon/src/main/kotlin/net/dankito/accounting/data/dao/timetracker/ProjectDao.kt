package net.dankito.accounting.data.dao.timetracker

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.timetracker.Project
import net.dankito.jpa.entitymanager.IEntityManager


class ProjectDao(entityManager: IEntityManager)
    : IProjectDao, CouchbaseBasedDao<Project>(Project::class.java, entityManager)