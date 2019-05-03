package net.dankito.accounting.service.banking

import net.dankito.accounting.data.dao.banking.IBankAccountDao
import net.dankito.accounting.data.dao.banking.IBankAccountTransactionDao
import net.dankito.accounting.data.dao.banking.IBankAccountTransactionsDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.*
import net.dankito.accounting.data.model.event.BankAccountAddedEvent
import net.dankito.utils.events.IEventBus


open class BankAccountService(private val bankingClient: IBankingClient,
                              private val bankAccountDao: IBankAccountDao,
                              private val transactionsDao: IBankAccountTransactionsDao,
                              private val transactionDao: IBankAccountTransactionDao,
                              private val eventBus: IEventBus
) : IBankAccountService {

    protected var bankAccountsProperty: List<BankAccount>? = null

    protected var bankAccountTransactionsProperty: Set<BankAccountTransaction>? = null


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
        val isNewAccount = account.isPersisted()

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

        bankAccountTransactionsProperty = transactions.toSet()

        return transactions
    }

    // TODO: update bankAccountTransactionsProperty
    override fun saveOrUpdateTransaction(transaction: BankAccountTransaction) {
        transactionDao.saveOrUpdate(transaction)
    }

    override fun saveOrUpdateTransactions(transactions: List<BankAccountTransaction>) {
        transactionDao.saveOrUpdate(transactions)
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
                                                       callback: (Set<BankAccountTransaction>) -> Unit) {
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
        getAccountTransactionsAsync(account) { getAccountTransactionsResult ->
            getAccountTransactionsResult.transactions?.let { bankAccountTransactions ->

                updateAccountAndTransactionDataInDb(account, bankAccountTransactions)

                (bankAccountTransactionsProperty as? MutableSet)?.addAll(bankAccountTransactions.transactions)

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

                // then if either usage contains document number ...
                document.documentNumber?.let { documentNumber ->
                    if (transaction.usage.contains(documentNumber, true)) {
                        return transaction
                    }
                }

                // or sender matches recipient
                document.recipient?.name?.let { recipientName ->
                    if (transaction.showSenderOrReceiver &&
                        (transaction.senderOrReceiverName.contains(recipientName, true) ||
                                recipientName.contains(transaction.senderOrReceiverName))) {
                        return transaction
                    }
                }
            }
        }

        return null
    }

    protected open fun doAmountsMatch(invoice: Document, transaction: BankAccountTransaction): Boolean {
        val transactionAmount = transaction.amount.toDouble()
        val diff = invoice.totalAmount - transactionAmount

        return Math.abs(diff) < 0.01 && Math.signum(invoice.totalAmount) == Math.signum(transactionAmount)
    }

}