package net.dankito.accounting.javafx.windows.banking

import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.controls.AccountingEntriesDetailsField
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.get
import tornadofx.vbox
import javax.inject.Inject


class BankAccountTransactionDetailsWindow(private val transaction: BankAccountTransaction) : Window() {


    @Inject
    lateinit var presenter: OverviewPresenter


    init {
        AppComponent.component.inject(this)
    }


    override val root = vbox {

        add(AccountingEntriesDetailsField(messages["bank.account.transaction.details.window.value"], presenter.getCurrencyString(transaction.amount)))

        add(AccountingEntriesDetailsField(messages["bank.account.transaction.details.window.type"], transaction.type))

        add(AccountingEntriesDetailsField(messages["bank.account.transaction.details.window.other.name"], transaction.senderOrReceiverName))

        add(AccountingEntriesDetailsField(messages["bank.account.transaction.details.window.other.iban.or.bank.code"], transaction.senderOrReceiverAccountNumber))

        add(AccountingEntriesDetailsField(messages["bank.account.transaction.details.window.other.bic.or.account.number"], transaction.senderOrReceiverBankCode))

        add(AccountingEntriesDetailsField(messages["bank.account.transaction.details.window.usage"], transaction.usage))

    }

}