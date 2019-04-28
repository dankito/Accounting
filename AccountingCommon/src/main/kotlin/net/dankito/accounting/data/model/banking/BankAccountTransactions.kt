package net.dankito.accounting.data.model.banking

import net.dankito.accounting.data.model.BaseEntity
import java.math.BigDecimal
import javax.persistence.*


@Entity
class BankAccountTransactions(

    @Column(name = "balance")
    var balance: BigDecimal,

    @OneToMany(fetch = FetchType.LAZY, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val transactions: List<BankAccountTransaction>

) : BaseEntity() {


    internal constructor() : this(BigDecimal.ZERO, listOf()) // for object deserializers


    override fun toString(): String {
        return "$balance, ${transactions.size} transactions"
    }

}