package net.dankito.accounting.data.model

import javax.persistence.Column
import javax.persistence.Entity


@Entity
class Person(

    @Column(name = FirstNameColumnName)
    var firstName: String,

    @Column(name = LastNameColumnName)
    var lastName: String,

    address: Address

) : NaturalOrLegalPerson(firstName + " " + lastName, address) {

    companion object {

        const val FirstNameColumnName = "first_name"

        const val LastNameColumnName = "last_name"

    }

    constructor() : this("", "", Address()) // for object deserializers


    /**
     * Returns "[firstName] [lastName]".
     */
    override var name: String
        get() = "$firstName $lastName"
        set (value) {
            val parts = value.split(' ')

            if (parts.size > 1) {
                firstName = parts.subList(0, parts.size - 1).joinToString(" ")
                lastName = parts.last()
            }
            else {
                firstName = ""
                lastName = parts[0]
            }
        }


    override fun toString(): String {
        return name
    }

}