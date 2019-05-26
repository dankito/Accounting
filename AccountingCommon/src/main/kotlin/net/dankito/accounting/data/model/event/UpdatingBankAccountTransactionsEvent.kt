package net.dankito.accounting.data.model.event

import net.dankito.accounting.data.model.banking.BankAccount


class UpdatingBankAccountTransactionsEvent(val accounts: List<BankAccount>) : IEvent {

    constructor(account: BankAccount) : this(listOf(account))

}