package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.Document


interface IDocumentDao {

    fun saveOrUpdate(document: Document)

    fun getAll(): List<Document>

}