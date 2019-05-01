package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.banking.CheckBankAccountCredentialsResult
import net.dankito.accounting.data.model.filter.StringFilter
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


    fun filterTransactions(filters: List<StringFilter<BankAccountTransaction>>): List<BankAccountTransaction> {
        return collectionFilter.filterStringField(accountService.getAccountTransactions(), filters).toList()
    }


    fun createDocumentsForTransactions(transactions: List<BankAccountTransaction>, valueAddedTaxRate: Float)
            : List<Document> {

        return transactions.map { transaction ->
            mapTransactionToDocument(transaction, valueAddedTaxRate)
        }
    }

    private fun mapTransactionToDocument(transaction: BankAccountTransaction, valueAddedTaxRate: Float): Document {
        val type = if (transaction.isDebit) DocumentType.Expenditure else DocumentType.Revenue

        val document = Document(type, Math.abs(transaction.amount.toDouble()), valueAddedTaxRate)

        document.paymentDate = transaction.valueDate
        document.description = transaction.senderOrReceiverName + " - " + transaction.usage

        return document
    }


    fun showTransactionDetailsWindow(transaction: BankAccountTransaction) {
        router.showBankAccountTransactionDetailsWindow(transaction)
    }

    fun showEditAutomaticAccountTransactionImportWindow() {
        router.showEditAutomaticAccountTransactionImportWindow()
    }

}