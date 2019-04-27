package net.dankito.accounting.data.model

import javax.persistence.Entity


@Entity
class Company(name: String, address: Address) : NaturalOrLegalPerson(name, address) {

    internal constructor() : this("", Address()) // for object deserializers
}