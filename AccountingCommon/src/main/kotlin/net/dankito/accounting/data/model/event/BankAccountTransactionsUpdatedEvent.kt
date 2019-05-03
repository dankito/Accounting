package net.dankito.accounting.data.model.event

import net.dankito.accounting.data.model.banking.BankAccountTransaction


class BankAccountTransactionsUpdatedEvent(val updatedTransactions: List<BankAccountTransaction>) : IEvent {

    constructor(updatedTransaction: BankAccountTransaction) : this(listOf(updatedTransaction))

}