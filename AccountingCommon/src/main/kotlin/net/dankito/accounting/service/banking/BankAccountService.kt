package net.dankito.accounting.service.banking

import net.dankito.accounting.data.dao.banking.IBankAccountDao
import net.dankito.accounting.data.dao.banking.IBankAccountTransactionDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.*
import net.dankito.accounting.data.model.event.BankAccountAddedEvent
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.event.UpdatingBankAccountTransactionsEvent
import net.dankito.utils.events.IEventBus


open class BankAccountService(private val bankingClient: IBankingClient,
                              private val bankAccountDao: IBankAccountDao,
                              private val transactionDao: IBankAccountTransactionDao,
                              private val eventBus: IEventBus
) : IBankAccountService {

    companion object {
        const val AllowedAmountDifferenceInPercent = 0.001
    }


    protected var bankAccountsProperty: List<BankAccount>? = null

    protected var bankAccountTransactionsProperty: MutableSet<BankAccountTransaction>? = null


    override fun getBankAccounts(): List<BankAccount> {
        bankAccountsProperty?.let {
            return it
        }

        val bankAccounts = bankAccountDao.getAll()

        this.bankAccountsProperty = bankAccounts

        return bankAccounts
    }

    override fun checkAccountCredentialsAsync(account: BankAccount, callback: (CheckBankAccountCredentialsResult) -> Unit) {
        bankingClient.checkAccountCredentialsAsync(account, callback)
    }

    override fun saveOrUpdateAccount(account: BankAccount) {
        val isNewAccount = account.isPersisted() == false

        bankAccountDao.saveOrUpdate(account)

        if (isNewAccount) {
            (bankAccountsProperty as? MutableList)?.add(account)

            eventBus.post(BankAccountAddedEvent(account))
        }
    }


    override fun getAccountTransactions(): List<BankAccountTransaction> {
        bankAccountTransactionsProperty?.let {
            return it.toList()
        }

        val transactions = transactionDao.getAll()

        bankAccountTransactionsProperty = transactions.toMutableSet()

        return transactions
    }

    // TODO: update bankAccountTransactionsProperty
    override fun saveOrUpdateTransaction(transaction: BankAccountTransaction) {
        transactionDao.saveOrUpdate(transaction)

        postTransactionUpdatedEvent(transaction)
    }

    override fun saveOrUpdateTransactions(transactions: List<BankAccountTransaction>) {
        transactionDao.saveOrUpdate(transactions)

        postTransactionUpdatedEvent(transactions)
    }

    private fun postTransactionUpdatedEvent(updatedTransaction: BankAccountTransaction) {
        postTransactionUpdatedEvent(listOf(updatedTransaction))
    }

    private fun postTransactionUpdatedEvent(updatedTransactions: List<BankAccountTransaction>) {
        eventBus.post(BankAccountTransactionsUpdatedEvent(updatedTransactions))
    }


    override fun updateAccountsTransactionsAsync(callback: (List<BankAccountTransaction>) -> Unit) {
        val accounts = getBankAccounts()

        if (accounts.isNotEmpty()) {
            updateAccountsTransactionsAsync(accounts) { accountTransactionsSet ->
                bankAccountTransactionsProperty = accountTransactionsSet

                callback(accountTransactionsSet.toList())
            }
        }
        else {
            callback(getAccountTransactions())
        }
    }

    protected open fun updateAccountsTransactionsAsync(accounts: List<BankAccount>,
                                                       callback: (MutableSet<BankAccountTransaction>) -> Unit) {
        val countAccountsToRetrieve = accounts.size
        var retrievedAccounts = 0
        val allAccountTransactions = getAccountTransactions().toMutableSet()

        accounts.forEach { account ->
            updateAccountTransactionsAsync(account) { getAccountTransactionsResult ->

                getAccountTransactionsResult.transactions?.let { bankAccountTransactions ->

                    allAccountTransactions.addAll(bankAccountTransactions.transactions)

                }

                retrievedAccounts++

                if (retrievedAccounts == countAccountsToRetrieve) {
                    callback(allAccountTransactions)
                }
            }
        }
    }

    override fun updateAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit) {
        eventBus.post(UpdatingBankAccountTransactionsEvent(account))

        getAccountTransactionsAsync(account) { getAccountTransactionsResult ->
            getAccountTransactionsResult.transactions?.let { bankAccountTransactions ->

                bankAccountTransactionsProperty?.addAll(bankAccountTransactions.transactions)
                if (bankAccountTransactionsProperty == null) bankAccountTransactionsProperty = bankAccountTransactions.transactions.toMutableSet()
                bankAccountTransactionsProperty?.let { postTransactionUpdatedEvent(it.toList()) }

                updateAccountAndTransactionDataInDb(account, bankAccountTransactions)

            }

            callback(getAccountTransactionsResult)

        }
    }

    protected open fun updateAccountAndTransactionDataInDb(account: BankAccount,
                                                      bankAccountTransactions: BankAccountTransactions) {

        // Set implementation of CouchbaseLite JPA doesn't work
        val accountTransactions = HashSet(account.transactions)
        accountTransactions.addAll(bankAccountTransactions.transactions)

        account.transactions.clear()
        account.transactions.addAll(accountTransactions)
        saveOrUpdateTransactions(account.transactions.toList())

        account.balance = bankAccountTransactions.balance
        saveOrUpdateAccount(account)
    }

    override fun getAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit) {
        bankingClient.getAccountTransactionsAsync(account, callback)
    }

    override fun findAccountTransactionThatMatchesDocument(document: Document): BankAccountTransaction? {
        getAccountTransactions().forEach { transaction ->

            // first check if amounts match
            if (doAmountsMatch(document, transaction)) {
                if (accountTransactionsMatchesDocument(document, transaction)) {
                    return transaction
                }
            }
            else if (amountDifferOnlyByPercent(document, transaction, AllowedAmountDifferenceInPercent)) {
                if (accountTransactionsMatchesDocument(document, transaction)) {
                    return transaction
                }
            }
        }

        return null
    }

    protected open fun doAmountsMatch(document: Document, transaction: BankAccountTransaction): Boolean {
        val transactionAmount = transaction.amount.toDouble()
        val diff = document.totalAmount - transactionAmount

        return Math.abs(diff) < 0.01 && Math.signum(document.totalAmount) == Math.signum(transactionAmount)
    }

    protected open fun amountDifferOnlyByPercent(document: Document, transaction: BankAccountTransaction,
                                                 allowedAmountDifferenceInPercent: Double): Boolean {

        val transactionAmount = transaction.amount.toDouble()
        val percent = document.totalAmount / transactionAmount

        return percent in (1 - allowedAmountDifferenceInPercent)..(1 + allowedAmountDifferenceInPercent)
    }

    protected open fun accountTransactionsMatchesDocument(document: Document, transaction: BankAccountTransaction): Boolean {
        return transactionUsageContainsDocumentNumber(document, transaction)
                || transactionSenderIsDocumentRecipient(document, transaction)
    }

    protected open fun transactionUsageContainsDocumentNumber(document: Document, transaction: BankAccountTransaction): Boolean {
        document.documentNumber?.let { documentNumber ->
            if (transaction.usage.contains(documentNumber, true)) {
                return true
            }
        }

        return false
    }

    protected open fun transactionSenderIsDocumentRecipient(document: Document, transaction: BankAccountTransaction): Boolean {
        document.recipient?.name?.let { recipientName ->
            if (transaction.showSenderOrReceiver &&
                (transaction.senderOrReceiverName.contains(recipientName, true) ||
                        recipientName.contains(transaction.senderOrReceiverName))
            ) {
                return true
            }
        }

        return false
    }

}