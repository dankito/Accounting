package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.DocumentItem
import net.dankito.jpa.entitymanager.IEntityManager


class DocumentItemDao(entityManager: IEntityManager)
    : IDocumentItemDao, CouchbaseBasedDao<DocumentItem>(DocumentItem::class.java, entityManager)