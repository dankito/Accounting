package net.dankito.accounting.data.model.tax

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity


@Entity
class FederalState(

    @Column(name = NameColumnName)
    val name: String,

    @Column(name = FederalStateIdColumnName)
    val federalStateId: Int

) : BaseEntity() {

    companion object {

        const val NameColumnName = "name"

        const val FederalStateIdColumnName = "federal_state_id"

        const val FederalStateIdUnset = -1

    }


    internal constructor() : this("", FederalStateIdUnset) // for object deserializers


    override fun toString(): String {
        return name
    }

}