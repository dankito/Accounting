package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.GetAccountTransactionsResult


interface IBankingClient {

    fun getAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit)

}