package net.dankito.accounting.data.model.tax

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.*


@Entity
class FederalState(

    @Column(name = NameColumnName)
    val name: String,

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


    override fun toString(): String {
        return name
    }

}