package net.dankito.accounting.javafx.windows.mainwindow.controls

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter


class RevenuesOverview(private val presenter: OverviewPresenter)
    : DocumentsOverview("main.window.tab.overview.revenues.label") {


    override fun retrieveDocuments(): List<Document> {
        return presenter.getRevenues()
    }

}