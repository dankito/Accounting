package net.dankito.accounting.data.model


class Address(var street: String,
              var streetNumber: String,
              var zipCode: String,
              var city: String,
              var country: String
) : BaseEntity() {

    protected constructor() : this("", "", "", "", "") // for Jackson, ...

}