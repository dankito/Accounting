package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.presenter.OverviewPresenter


class RevenuesOverview(presenter: OverviewPresenter)
    : DocumentsOverview("revenues", presenter) {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getRevenues()
    }

    override fun showCreateNewDocumentWindow() {
        presenter.showCreateRevenueWindow()
    }

}