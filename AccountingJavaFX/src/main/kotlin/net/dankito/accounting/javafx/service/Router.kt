package net.dankito.accounting.javafx.service

import javafx.stage.Modality
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.invoice.InvoiceData
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.BankAccountTransactionDetailsWindow
import net.dankito.accounting.javafx.windows.banking.EditAutomaticAccountTransactionImportWindow
import net.dankito.accounting.javafx.windows.banking.EditBankAccountWindow
import net.dankito.accounting.javafx.windows.document.EditDocumentWindow
import net.dankito.accounting.javafx.windows.invoice.CreateInvoiceWindow
import net.dankito.accounting.javafx.windows.person.EditPersonWindow
import net.dankito.accounting.javafx.windows.person.model.RequiredField
import net.dankito.accounting.javafx.windows.timetracker.EditTimeTrackerAccountWindow
import tornadofx.FX.Companion.messages
import tornadofx.get


class Router {


    fun showEditDocumentWindow(document: Document, presenter: OverviewPresenter, extractedData: InvoiceData? = null) {
        EditDocumentWindow(document, presenter, extractedData).show()
    }

    fun showCreateInvoiceWindow() {
        CreateInvoiceWindow().show()
    }

    fun showEditTimeTrackerAccountWindow(account: TimeTrackerAccount, userDidEditTimeTrackerAccountCallback: ((Boolean) -> Unit)? = null) {
        EditTimeTrackerAccountWindow(account, userDidEditTimeTrackerAccountCallback).show()
    }

    fun showEditPersonWindow(person: NaturalOrLegalPerson?, personType: PersonType, requiredFields: List<RequiredField>, didUserSavePersonCallback: ((Boolean, NaturalOrLegalPerson?) -> Unit)? = null) {
        EditPersonWindow(person, personType, requiredFields, didUserSavePersonCallback).show()
    }


    fun showEditBankAccountWindow(bankAccount: BankAccount) {
        EditBankAccountWindow(bankAccount).show(messages["edit.bank.account.window.title"])
    }

    fun showBankAccountTransactionDetailsWindow(transaction: BankAccountTransaction) {
        BankAccountTransactionDetailsWindow(transaction).show(messages["bank.account.transaction.details.window.title"],
            modality = Modality.WINDOW_MODAL)
    }

    fun showEditAutomaticAccountTransactionImportWindow() {
        EditAutomaticAccountTransactionImportWindow().show()
    }

}