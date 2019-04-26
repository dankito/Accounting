package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Document
import net.dankito.jpa.entitymanager.IEntityManager


class DocumentDao(entityManager: IEntityManager)
    : IDocumentDao, CouchbaseBasedDao<Document>(Document::class.java, entityManager)