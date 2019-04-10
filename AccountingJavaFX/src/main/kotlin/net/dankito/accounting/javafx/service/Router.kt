package net.dankito.accounting.javafx.service

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.windows.document.EditDocumentWindow
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter
import tornadofx.Component


class Router(private val rootView: Component) {


    fun showEditDocumentWindow(document: Document, presenter: OverviewPresenter) {
        EditDocumentWindow(document, presenter).show()
    }

}