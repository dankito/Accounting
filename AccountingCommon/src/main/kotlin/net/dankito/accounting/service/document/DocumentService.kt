package net.dankito.accounting.service.document

import net.dankito.accounting.data.dao.IDocumentDao
import net.dankito.accounting.data.dao.IDocumentItemDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState


open class DocumentService(protected val dao: IDocumentDao, protected val documentItemDao: IDocumentItemDao) : IDocumentService {


    override fun saveOrUpdate(document: Document) {
        // TODO: how to get removed DocumentItems and delete them from database?
        document.items.forEach { item ->
            documentItemDao.saveOrUpdate(item)
        }

        dao.saveOrUpdate(document)
    }


    override fun getRevenues(): List<Document> {
        return getAll().filter { it.type == DocumentType.Revenue && it.paymentState == PaymentState.Paid }
    }

    override fun getExpenditures(): List<Document> {
        return getAll().filter { it.type == DocumentType.Expenditure } // TODO: also check if already paid?
    }

    override fun getCreatedInvoices(): List<Document> {
        return getAll().filter { it.isSelfCreatedInvoice }
    }


    protected open fun getAll() = dao.getAll().toMutableList()

}