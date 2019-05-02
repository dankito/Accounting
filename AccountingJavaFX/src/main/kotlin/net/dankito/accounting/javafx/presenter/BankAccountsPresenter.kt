package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.banking.CheckBankAccountCredentialsResult
import net.dankito.accounting.data.model.filter.Filter
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.filter.ICollectionFilter


class BankAccountsPresenter(private val accountService: IBankAccountService,
                            private val collectionFilter: ICollectionFilter,
                            private val router: Router) {


    fun checkAccountCredentialsAsync(account: BankAccount, callback: (CheckBankAccountCredentialsResult) -> Unit) {
        accountService.checkAccountCredentialsAsync(account, callback)
    }

    fun saveAccountAndFetchTransactions(account: BankAccount) {
        accountService.saveOrUpdateAccount(account)

        accountService.updateAccountTransactionsAsync(account) { }
    }


    fun getAccountTransactions(): List<BankAccountTransaction> {
        return accountService.getAccountTransactions()
    }

    fun updateAccountsTransactionsAsync(callback: (List<BankAccountTransaction>) -> Unit) {
        accountService.updateAccountsTransactionsAsync(callback)
    }


    fun filterTransactions(filters: List<Filter>): List<BankAccountTransaction> {
        return collectionFilter.filter(filters, accountService.getAccountTransactions()).toList()
    }


    fun showTransactionDetailsWindow(transaction: BankAccountTransaction) {
        router.showBankAccountTransactionDetailsWindow(transaction)
    }

    fun showEditAutomaticAccountTransactionImportWindow() {
        router.showEditAutomaticAccountTransactionImportWindow()
    }

}