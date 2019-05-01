package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.CheckBankAccountCredentialsResult
import net.dankito.accounting.data.model.banking.GetAccountTransactionsResult


interface IBankingClient {

    fun checkAccountCredentialsAsync(account: BankAccount, callback: (CheckBankAccountCredentialsResult) -> Unit)

    fun getAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit)

}