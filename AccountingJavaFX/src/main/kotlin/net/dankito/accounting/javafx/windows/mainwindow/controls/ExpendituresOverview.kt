package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter


class ExpendituresOverview(presenter: OverviewPresenter)
    : DocumentsOverview("expenditures", presenter) {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getExpenditures()
    }

    override fun showCreateNewDocumentWindow() {
        presenter.showCreateExpenditureWindow()
    }

}