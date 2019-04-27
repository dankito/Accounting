package net.dankito.accounting.data.model

import javax.persistence.*


@MappedSuperclass
abstract class NaturalOrLegalPerson(

    @Column(name = NameColumnName)
    open var name: String,


    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn(name = AddressJoinColumnName)
    var address: Address

) : BaseEntity() {

    companion object {

        const val NameColumnName = "name"

        const val AddressJoinColumnName = "address"

    }

    constructor() : this("", Address("", "", "", "", "")) // for object deserializers


    override fun toString(): String {
        return name
    }

}