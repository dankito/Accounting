package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction


interface IBankAccountService {

    fun getBankAccounts(): List<BankAccount>

    fun saveOrUpdateAccount(account: BankAccount)


    fun getAccountTransactions(): List<BankAccountTransaction>

    fun saveOrUpdateTransaction(transaction: BankAccountTransaction)

    fun saveOrUpdateTransactions(transactions: List<BankAccountTransaction>)


    fun updateAccountTransactionsAsync(callback: (List<BankAccountTransaction>) -> Unit)

    fun getAccountTransactionsAsync(account: BankAccount, callback: (List<BankAccountTransaction>) -> Unit)


    fun findAccountTransactionThatMatchesDocument(document: Document): BankAccountTransaction?

}