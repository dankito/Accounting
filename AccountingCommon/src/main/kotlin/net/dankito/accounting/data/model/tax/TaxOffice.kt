package net.dankito.accounting.data.model.tax

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity


@Entity
class TaxOffice(

    @Column(name = NameColumnName)
    val name: String,

    @Column(name = TaxOfficeIdColumnName)
    val taxOfficeId: Int

) : BaseEntity() {

    companion object {

        const val NameColumnName = "name"

        const val TaxOfficeIdColumnName = "tax_office_id"

        const val TaxOfficeIdUnset = -1

    }


    internal constructor() : this("", TaxOfficeIdUnset) // for object deserializers


    override fun toString(): String {
        return name
    }

}