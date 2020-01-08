package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.invoice.InvoiceData
import net.dankito.accounting.javafx.presenter.OverviewPresenter


class ExpendituresOverview(presenter: OverviewPresenter)
    : DocumentsOverview("expenditures", presenter) {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getExpenditures()
    }

    override fun showCreateNewDocumentWindow(extractedData: InvoiceData?) {
        presenter.showCreateExpenditureWindow(extractedData)
    }

}