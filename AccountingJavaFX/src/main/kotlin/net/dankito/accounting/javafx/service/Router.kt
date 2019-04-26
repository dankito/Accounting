package net.dankito.accounting.javafx.service

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.javafx.presenter.EditPersonPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.document.EditDocumentWindow
import net.dankito.accounting.javafx.windows.person.EditPersonWindow
import tornadofx.Component


class Router(private val rootView: Component) {


    fun showEditDocumentWindow(document: Document, presenter: OverviewPresenter) {
        EditDocumentWindow(document, presenter).show()
    }

    fun showEditPersonWindow(person: Person, presenter: EditPersonPresenter,
                             didUserSavePersonCallback: ((Boolean) -> Unit)? = null) {
        EditPersonWindow(person, presenter, didUserSavePersonCallback).show()
    }

}