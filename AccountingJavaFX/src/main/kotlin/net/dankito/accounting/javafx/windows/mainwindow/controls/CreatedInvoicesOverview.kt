package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.presenter.OverviewPresenter


class CreatedInvoicesOverview(overviewPresenter: OverviewPresenter)
    : DocumentsOverview("created.invoices", overviewPresenter) {


    override val styleDocumentsFromCurrentAndPreviousPeriod = false

    override fun retrieveDocuments(): List<Document> {
        return presenter.getCreatedInvoices()
    }

    override fun sortDocuments(retrievedDocuments: List<Document>): List<Document> {
        return retrievedDocuments.sortedByDescending { it.issueDate }
    }

    override fun showCreateNewDocumentWindow() {
        presenter.showCreateInvoiceWindow()
    }

}