package net.dankito.accounting.data.model.event

import net.dankito.accounting.data.model.banking.BankAccount


class BankAccountAddedEvent(val bankAccount: BankAccount) : IEvent {

    override fun toString(): String {
        return bankAccount.toString()
    }

}