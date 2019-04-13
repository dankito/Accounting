package net.dankito.accounting.data.model


class Person(var firstName: String,
             var lastName: String,
             var primaryAddress: Address
) {

    constructor() : this("", "", Address("", "", "", "", ""))


    /**
     * Returns "[firstName] [lastName]".
     */
    val name: String
        get() = "$firstName $lastName"


    override fun toString(): String {
        return name
    }

}