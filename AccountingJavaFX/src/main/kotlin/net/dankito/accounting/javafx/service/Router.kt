package net.dankito.accounting.javafx.service

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.document.EditDocumentWindow
import net.dankito.accounting.javafx.windows.invoice.CreateInvoiceWindow
import net.dankito.accounting.javafx.windows.person.EditPersonWindow
import net.dankito.accounting.javafx.windows.timetracker.EditTimeTrackerAccountWindow


class Router {


    fun showEditDocumentWindow(document: Document, presenter: OverviewPresenter) {
        EditDocumentWindow(document, presenter).show()
    }

    fun showCreateInvoiceWindow() {
        CreateInvoiceWindow().show()
    }

    fun showEditTimeTrackerAccountWindow(account: TimeTrackerAccount, userDidEditTimeTrackerAccountCallback: ((Boolean) -> Unit)? = null) {
        EditTimeTrackerAccountWindow(account, userDidEditTimeTrackerAccountCallback).show()
    }

    fun showEditPersonWindow(person: Person, didUserSavePersonCallback: ((Boolean) -> Unit)? = null) {
        EditPersonWindow(person, didUserSavePersonCallback).show()
    }

}