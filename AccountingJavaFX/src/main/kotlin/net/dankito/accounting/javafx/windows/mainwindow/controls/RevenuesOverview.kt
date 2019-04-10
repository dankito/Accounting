package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter


class RevenuesOverview(presenter: OverviewPresenter)
    : DocumentsOverview("main.window.tab.overview.revenues.label", presenter) {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getRevenues()
    }

    override fun showCreateNewDocumentWindow() {
        presenter.showCreateRevenueWindow()
    }

}