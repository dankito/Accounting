package net.dankito.accounting.data.model.person

import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.*


@MappedSuperclass
abstract class NaturalOrLegalPerson(

    @Column(name = NameColumnName)
    open var name: String,

    @Column(name = TypeColumnName)
    var type: PersonType,


    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn(name = AddressJoinColumnName)
    var address: Address

) : BaseEntity() {

    companion object {

        const val NameColumnName = "name"

        const val TypeColumnName = "type"

        const val AddressJoinColumnName = "address"

    }

    constructor() : this("", PersonType.Client, Address()) // for object deserializers


    override fun toString(): String {
        return name
    }

}