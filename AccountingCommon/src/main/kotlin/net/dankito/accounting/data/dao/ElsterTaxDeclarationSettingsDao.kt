package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.tax.elster.ElsterTaxDeclarationSettings
import net.dankito.jpa.entitymanager.IEntityManager


class ElsterTaxDeclarationSettingsDao(entityManager: IEntityManager)
    : IElsterTaxDeclarationSettingsDao, CouchbaseBasedDao<ElsterTaxDeclarationSettings>(ElsterTaxDeclarationSettings::class.java, entityManager)