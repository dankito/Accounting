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


    val isCredit: Boolean
        get() = !!! isDebit

    val isDebit: Boolean
        get() = amount.signum() < 0



    override fun toString(): String {
        return "$amount $senderOrReceiverName: $usage"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BankAccountTransaction) return false

        if (valueDate != other.valueDate) return false
        if (usage != other.usage) return false
        if (amount.compareTo(other.amount) != 0) return false
        if (senderOrReceiverName != other.senderOrReceiverName) return false
        if (senderOrReceiverAccountNumber != other.senderOrReceiverAccountNumber) return false
        if (senderOrReceiverBankCode != other.senderOrReceiverBankCode) return false

        return true
    }

    override fun hashCode(): Int {
        // e.g. BigDecimal of "150.0" produces another hash code as BigDecimal of "-1.5E+2" even though their values are the same -> has to be converted to double before
        var result = amount.toDouble().hashCode()
        result = 31 * result + usage.hashCode()
        result = 31 * result + senderOrReceiverName.hashCode()
        result = 31 * result + senderOrReceiverAccountNumber.hashCode()
        result = 31 * result + senderOrReceiverBankCode.hashCode()
        result = 31 * result + valueDate.hashCode()
        return result
    }

}