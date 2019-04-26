package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.settings.AppSettings
import net.dankito.jpa.entitymanager.IEntityManager


class AppSettingsDao(entityManager: IEntityManager)
    : IAppSettingsDao, CouchbaseBasedDao<AppSettings>(AppSettings::class.java, entityManager)