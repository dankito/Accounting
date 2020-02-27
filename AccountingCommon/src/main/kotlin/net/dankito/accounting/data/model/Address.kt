package net.dankito.accounting.data.model

import javax.persistence.Column
import javax.persistence.Entity


@Entity
class Address(

    @Column(name = StreetColumnName)
    var street: String,

    @Column(name = StreetNumberColumnName)
    var streetNumber: String,

    @Column(name = ZipCodeColumnName)
    var zipCode: String,

    @Column(name = CityColumnName)
    var city: String,

    @Column(name = CountryColumnName)
    var country: String

) : BaseEntity() {

    companion object {

        const val StreetColumnName = "street"

        const val StreetNumberColumnName = "street_number"

        const val ZipCodeColumnName = "zip_code"

        const val CityColumnName = "city"

        const val CountryColumnName = "country"

    }


    internal constructor() : this("", "", "", "", "") // for object deserializers


    override fun toString(): String {
        return "$street $streetNumber $zipCode $city"
    }

}