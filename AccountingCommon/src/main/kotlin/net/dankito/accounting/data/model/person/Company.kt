package net.dankito.accounting.data.model.person

import net.dankito.accounting.data.model.Address
import javax.persistence.Entity


@Entity
class Company(name: String, type: PersonType, address: Address) : NaturalOrLegalPerson(name, type, address) {

    internal constructor() : this("", PersonType.Client, Address()) // for object deserializers
}