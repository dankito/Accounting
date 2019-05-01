package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.banking.CheckBankAccountCredentialsResult
import net.dankito.accounting.data.model.banking.GetAccountTransactionsResult


interface IBankAccountService {

    fun getBankAccounts(): List<BankAccount>

    fun checkAccountCredentialsAsync(account: BankAccount, callback: (CheckBankAccountCredentialsResult) -> Unit)

    fun saveOrUpdateAccount(account: BankAccount)


    fun getAccountTransactions(): List<BankAccountTransaction>

    fun saveOrUpdateTransaction(transaction: BankAccountTransaction)

    fun saveOrUpdateTransactions(transactions: List<BankAccountTransaction>)


    fun updateAccountsTransactionsAsync(callback: (List<BankAccountTransaction>) -> Unit)

    fun updateAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit)

    fun getAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit)


    fun findAccountTransactionThatMatchesDocument(document: Document): BankAccountTransaction?

}