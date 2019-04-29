package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.service.banking.IBankAccountService


class BankAccountsPresenter(private val accountService: IBankAccountService) {

    fun getAccountTransactions(): List<BankAccountTransaction> {
        return accountService.getAccountTransactions()
    }

    fun updateAccountTransactionsAsync(callback: (List<BankAccountTransaction>) -> Unit) {
        accountService.updateAccountTransactionsAsync(callback)
    }

}