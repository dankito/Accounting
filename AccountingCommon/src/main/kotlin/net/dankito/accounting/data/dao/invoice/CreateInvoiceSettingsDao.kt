package net.dankito.accounting.data.dao.invoice

import net.dankito.accounting.data.dao.CouchbaseBasedDao
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import net.dankito.jpa.entitymanager.IEntityManager


class CreateInvoiceSettingsDao(entityManager: IEntityManager)
    : ICreateInvoiceSettingsDao, CouchbaseBasedDao<CreateInvoiceSettings>(CreateInvoiceSettings::class.java, entityManager)