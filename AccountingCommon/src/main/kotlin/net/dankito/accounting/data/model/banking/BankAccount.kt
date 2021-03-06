package net.dankito.accounting.data.model.banking

import net.dankito.accounting.data.model.BaseEntity
import java.math.BigDecimal
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
class BankAccount @JvmOverloads constructor(

    @Column(name = "bank_code")
    var bankCode: String,

    @Column(name = "customer_id")
    var customerId: String,

    @Column(name = "password")
    var password: String,

    @Column(name = "balance")
    var balance: BigDecimal = BigDecimal.ZERO,

    @OneToMany
    var transactions: MutableSet<BankAccountTransaction> = mutableSetOf(),

    @Column(name = "last_updated")
    var lastUpdated: Date? = null

) : BaseEntity() {


    internal constructor() : this("", "", "") // for object deserializers


    override fun toString(): String {
        return "$bankCode $customerId"
    }

}