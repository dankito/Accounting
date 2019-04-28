package net.dankito.accounting.data.model.banking

import net.dankito.accounting.data.model.BaseEntity
import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity


@Entity
class BankAccountTransaction(

    @Column
    val amount: BigDecimal,

    @Column
    val usage1: String,

    @Column
    val usage2: String?,

    @Column(columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    val showSenderOrReceiver: Boolean,

    @Column
    val senderOrReceiver: String,

    @Column
    val bookingDate: Date

) : BaseEntity() {


    internal constructor() : this(BigDecimal.ZERO, "", null, false, "", Date()) // for object deserializers


    val usage: String
        get() = usage1 + (usage2?.let { it } ?: "")


    override fun toString(): String {
        return "$amount $senderOrReceiver: $usage"
    }

}