package net.dankito.accounting.data.model.tax

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.*


@Entity
class FederalState(

    @Column(name = NameColumnName)
    var name: String,

    @Column(name = FederalStateIdColumnName)
    val federalStateId: Int,

    @OneToMany(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val taxOffices: List<TaxOffice>

) : BaseEntity() {

    companion object {

        const val NameColumnName = "name"

        const val FederalStateIdColumnName = "federal_state_id"

        const val FederalStateIdUnset = -1

    }


    internal constructor() : this("", FederalStateIdUnset, listOf()) // for object deserializers


    fun setTaxOffices(taxOffices: List<TaxOffice>) {
        (this.taxOffices as? MutableList)?.let { mutableTaxOffices ->
            mutableTaxOffices.clear()

            mutableTaxOffices.addAll(taxOffices)
        }
    }


    override fun toString(): String {
        return name
    }

}