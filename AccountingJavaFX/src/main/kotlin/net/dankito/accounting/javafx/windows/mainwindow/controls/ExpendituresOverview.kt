package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter


class ExpendituresOverview(presenter: OverviewPresenter)
    : DocumentsOverview("main.window.tab.overview.expenditures.label", presenter) {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getExpenditures()
    }

    override fun showCreateNewDocumentWindow() {
        presenter.showCreateExpenditureWindow()
    }

}