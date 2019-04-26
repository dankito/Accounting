package net.dankito.accounting.data.model

import javax.persistence.*


@Entity
class Person(

    @Column(name = FirstNameColumnName)
    var firstName: String,

    @Column(name = LastNameColumnName)
    var lastName: String,


    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn(name = PrimaryAddressJoinColumnName)
    var primaryAddress: Address

) : BaseEntity() {

    companion object {

        const val FirstNameColumnName = "first_name"

        const val LastNameColumnName = "last_name"

        const val PrimaryAddressJoinColumnName = "address"

    }

    constructor() : this("", "", Address("", "", "", "", "")) // for object deserializers


    /**
     * Returns "[firstName] [lastName]".
     */
    val name: String
        @Transient get() = "$firstName $lastName"


    override fun toString(): String {
        return name
    }

}