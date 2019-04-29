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
    val usage: String,

    @Column(columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    val showSenderOrReceiver: Boolean,

    @Column
    val senderOrReceiverName: String,

    @Column
    val senderOrReceiverAccountNumber: String,

    @Column
    val senderOrReceiverBankCode: String,

    @Column
    val valueDate: Date,

    @Column
    val type: String,

    @Column
    val currency: String,

    @Column
    val balance: BigDecimal

) : BaseEntity() {


    internal constructor() : this(BigDecimal.ZERO, "", false, "", "", "", Date(), "", "", BigDecimal.ZERO) // for object deserializers


    override fun toString(): String {
        return "$amount $senderOrReceiverName: $usage"
    }

}