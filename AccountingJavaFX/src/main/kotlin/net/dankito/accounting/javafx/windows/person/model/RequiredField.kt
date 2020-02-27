package net.dankito.accounting.javafx.windows.person.model


enum class RequiredField {

    CompanyName,

    PersonFirstName,

    PersonLastName,

    AddressStreet,

    AddressStreetNumber,

    AddressZipCode,

    AddressCity,

    AddressCountry;


    companion object {

        val All: List<RequiredField> = values().toList()

        val AllWithoutCountry: List<RequiredField> = All.filter { it != AddressCountry }

    }

}