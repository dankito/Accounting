package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.banking.BankAccountTransactions
import net.dankito.accounting.data.model.banking.GetAccountTransactionsResult
import net.dankito.banking.Hbci4JavaBankingClient
import net.dankito.banking.model.AccountCredentials
import net.dankito.banking.model.AccountingEntries
import net.dankito.banking.model.AccountingEntry
import net.dankito.banking.model.BankInfo
import net.dankito.utils.io.FileUtils
import java.math.BigDecimal


open class HbciBankingClient : IBankingClient {

    override fun getAccountTransactionsAsync(account: BankAccount, callback: (GetAccountTransactionsResult) -> Unit) {
        val client = Hbci4JavaBankingClient(mapToAccountCredentials(account), FileUtils().getTempDir())

        client.getAccountsAsync { getAccountsResult ->
            getAccountsResult.bankInfo?.let { bankInfo ->
                getAccountTurnoversAsync(client, bankInfo, callback)
            }
            ?: callback(GetAccountTransactionsResult(false, null, getAccountsResult.error))
        }
    }

    protected open fun getAccountTurnoversAsync(client: Hbci4JavaBankingClient, bankInfo: BankInfo,
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
                ?: callback(GetAccountTransactionsResult(true, mapToAccountTransactions(accountingEntries), null))
            }
        }
    }


    protected open fun mapToAccountCredentials(account: BankAccount): AccountCredentials {
        return AccountCredentials(account.bankCode, account.customerId, account.password)
    }

    protected open fun mapToAccountTransactions(accountingEntries: AccountingEntries): BankAccountTransactions {
        return BankAccountTransactions(accountingEntries.saldo?.bigDecimalValue ?: BigDecimal.ZERO,
            accountingEntries.entries.map { mapToTransaction(it) }
        )
    }

    protected open fun mapToTransaction(entry: AccountingEntry): BankAccountTransaction {
        return BankAccountTransaction(entry.value.bigDecimalValue,
            entry.getUsage1(), entry.getUsage2(),
            entry.showOtherName(), entry.other.name,
            entry.bookingDate
        )
    }

}