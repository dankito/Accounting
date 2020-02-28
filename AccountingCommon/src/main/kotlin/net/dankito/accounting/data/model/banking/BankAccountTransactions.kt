package net.dankito.accounting.data.model.banking

import net.dankito.accounting.data.model.BaseEntity
import java.math.BigDecimal


class BankAccountTransactions(

    var balance: BigDecimal,

    val transactions: List<BankAccountTransaction>

) : BaseEntity() {


    internal constructor() : this(BigDecimal.ZERO, listOf()) // for object deserializers


    override fun toString(): String {
        return "$balance, ${transactions.size} transactions"
    }

}