package net.dankito.accounting.data.model.banking

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity


@Entity
class BankAccount(

    @Column(name = "bank_code")
    var bankCode: String,

    @Column(name = "customer_id")
    var customerId: String,

    @Column(name = "password")
    var password: String

) : BaseEntity() {


    internal constructor() : this("", "", "") // for object deserializers


    override fun toString(): String {
        return "$bankCode $customerId"
    }

}