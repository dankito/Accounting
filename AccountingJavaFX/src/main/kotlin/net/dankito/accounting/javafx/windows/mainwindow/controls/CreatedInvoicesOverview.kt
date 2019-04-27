package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.presenter.OverviewPresenter


class CreatedInvoicesOverview(overviewPresenter: OverviewPresenter)
    : DocumentsOverview("created.invoices", overviewPresenter) {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getCreatedInvoices()
    }

    override fun getDocumentsInCurrentAndPreviousAccountingPeriod(retrievedDocuments: List<Document>): List<Document> {
        return presenter.getUnpaidInvoicesAndInvoicesInCurrentAndPreviousAccountingPeriod(retrievedDocuments)
    }

    override fun showCreateNewDocumentWindow() {
        presenter.showCreateInvoiceWindow()
    }

}