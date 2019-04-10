package net.dankito.accounting.service.document

import net.dankito.accounting.data.dao.IDocumentDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState


open class DocumentService(protected val dao: IDocumentDao) : IDocumentService {


    override fun saveOrUpdate(document: Document) {
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