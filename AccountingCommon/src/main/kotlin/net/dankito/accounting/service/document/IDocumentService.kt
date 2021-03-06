package net.dankito.accounting.service.document

import net.dankito.accounting.data.model.Document


interface IDocumentService {

    fun saveOrUpdate(document: Document)

    fun delete(document: Document)


    fun getRevenues(): List<Document>

    fun getExpenditures(): List<Document>

    fun getCreatedInvoices(): List<Document>

    fun getUnpaidCreatedInvoices(): List<Document>

}