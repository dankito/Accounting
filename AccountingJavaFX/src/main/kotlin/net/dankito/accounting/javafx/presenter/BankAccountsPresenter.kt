package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.banking.IBankAccountService


class BankAccountsPresenter(private val accountService: IBankAccountService, private val router: Router) {

    fun getAccountTransactions(): List<BankAccountTransaction> {
        return accountService.getAccountTransactions()
    }

    fun updateAccountTransactionsAsync(callback: (List<BankAccountTransaction>) -> Unit) {
        accountService.updateAccountTransactionsAsync(callback)
    }


    fun showTransactionDetailsWindow(transaction: BankAccountTransaction) {
        router.showBankAccountTransactionDetailsWindow(transaction)
    }

    fun showEditAutomaticAccountTransactionImportWindow() {
        router.showEditAutomaticAccountTransactionImportWindow()
    }

}