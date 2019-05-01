package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.banking.*
import net.dankito.banking.Hbci4JavaBankingClient
import net.dankito.banking.model.AccountCredentials
import net.dankito.banking.model.AccountingEntries
import net.dankito.banking.model.AccountingEntry
import net.dankito.banking.model.BankInfo
import net.dankito.utils.io.FileUtils
import java.math.BigDecimal


open class HbciBankingClient : IBankingClient {

    override fun checkAccountCredentialsAsync(account: BankAccount, callback: (CheckBankAccountCredentialsResult) -> Unit) {
        val client = createClient(account)

        client.getAccountsAsync { result ->
            callback(CheckBankAccountCredentialsResult(result.successful, result.error))
        }
    }

    override fun getAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit) {
        val client = createClient(account)

        client.getAccountsAsync { getAccountsResult ->
            getAccountsResult.bankInfo?.let { bankInfo ->
                getAccountTransactionsAsync(client, account, bankInfo, callback)
            }
            ?: callback(GetAccountTransactionsResult(false, null, getAccountsResult.error))
        }
    }

    private fun createClient(account: BankAccount) =
        Hbci4JavaBankingClient(mapToAccountCredentials(account), FileUtils().getTempDir())

    protected open fun getAccountTransactionsAsync(client: Hbci4JavaBankingClient, account: BankAccount, bankInfo: BankInfo,
                                                   callback: (GetAccountTransactionsResult) -> Unit) {

        if (bankInfo.accounts.isEmpty()) {
            callback(GetAccountTransactionsResult(false, null, null))
        }
        else {
            // TODO: get transactions for all accounts not only for first one
            client.getAccountingEntriesAsync(bankInfo.accounts.first()) { accountingEntries ->
                accountingEntries.error?.let {
                    callback(GetAccountTransactionsResult(false, null, accountingEntries.error))
                }
                ?: callback(GetAccountTransactionsResult(true, mapToAccountTransactions(account, accountingEntries), null))
            }
        }
    }


    protected open fun mapToAccountCredentials(account: BankAccount): AccountCredentials {
        return AccountCredentials(account.bankCode, account.customerId, account.password)
    }

    protected open fun mapToAccountTransactions(account: BankAccount, accountingEntries: AccountingEntries): BankAccountTransactions {
        return BankAccountTransactions(accountingEntries.saldo?.bigDecimalValue ?: BigDecimal.ZERO,
            accountingEntries.entries.map { mapToTransaction(account, it) }
        )
    }

    protected open fun mapToTransaction(account: BankAccount, entry: AccountingEntry): BankAccountTransaction {
        val usage = entry.sepaVerwendungszweck ?: entry.usageWithNoSpecialType
                    ?: (entry.getUsage1() + (entry.getUsage2()?.let { it } ?: ""))

        val otherName = entry.other.name + if (entry.other.name2.isNullOrBlank()) "" else entry.other.name2
        val otherAccountNumber = if (entry.other.iban.isNullOrBlank()) entry.other.number else entry.other.iban
        val otherBankCode = if (entry.other.bic.isNullOrBlank()) entry.other.blz else entry.other.bic

        return BankAccountTransaction(entry.value.bigDecimalValue, usage,
            entry.showOtherName(), otherName, otherAccountNumber, otherBankCode,
            entry.valutaDate, entry.type, entry.value.curr, entry.saldo.bigDecimalValue, account
        )
    }

}