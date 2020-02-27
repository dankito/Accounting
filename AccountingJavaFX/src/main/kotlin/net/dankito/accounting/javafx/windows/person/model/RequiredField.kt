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

        val allWithoutCountry: List<RequiredField> = RequiredField.values().toList().filter { it != AddressCountry }

    }

}