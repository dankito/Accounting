package net.dankito.accounting.javafx.windows.mainwindow

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.service.document.DocumentService


class OverviewPresenter(private val documentService: DocumentService) {


    fun saveOrUpdate(document: Document) {
        documentService.saveOrUpdate(document)
    }


    fun getRevenues(): List<Document> {
        return documentService.getRevenues()
    }

    fun getExpenditures(): List<Document> {
        return documentService.getExpenditures()
    }

    fun getCreatedInvoices(): List<Document> {
        return documentService.getCreatedInvoices()
    }

}