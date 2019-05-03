package net.dankito.accounting.service.document

import net.dankito.accounting.data.dao.IDocumentDao
import net.dankito.accounting.data.dao.IDocumentItemDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState
import net.dankito.accounting.data.model.event.DocumentsUpdatedEvent
import net.dankito.utils.events.IEventBus


open class DocumentService(protected val dao: IDocumentDao, protected val documentItemDao: IDocumentItemDao,
                           private val eventBus: IEventBus) : IDocumentService {


    override fun saveOrUpdate(document: Document) {
        // TODO: how to get removed DocumentItems and delete them from database?
        document.items.forEach { item ->
            documentItemDao.saveOrUpdate(item)
        }

        dao.saveOrUpdate(document)

        postDocumentsUpdated()
    }

    override fun delete(document: Document) {
        document.items.forEach { item ->
            documentItemDao.delete(item)
        }

        dao.delete(document)

        postDocumentsUpdated()
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

    override fun getUnpaidCreatedInvoices(): List<Document> {
        return getCreatedInvoices().filter { it.paymentState != PaymentState.Paid }
    }


    protected open fun getAll() = dao.getAll().toMutableList()


    protected open fun postDocumentsUpdated() {
        eventBus.post(DocumentsUpdatedEvent())
    }

}