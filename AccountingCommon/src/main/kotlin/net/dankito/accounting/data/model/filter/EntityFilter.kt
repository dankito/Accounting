package net.dankito.accounting.data.model.filter

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
open class EntityFilter(

    @Column
    var name: String,

    @Column
    val classToFilter: String,

    @Column
    var valueAddedTaxRateForCreatedDocuments: Float = 0f,

    @Column
    var descriptionForCreatedDocuments: String,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val filterDefinitions: List<Filter>

) : BaseEntity() {


    constructor(name: String, classToFilter: Class<*>, valueAddedTaxRateForCreatedDocuments: Float, descriptionForCreatedDocuments: String, filterDefinitions: List<Filter>)
            : this(name, classToFilter.name, valueAddedTaxRateForCreatedDocuments, descriptionForCreatedDocuments, filterDefinitions)

    internal constructor() : this("", "", 0f, "", listOf()) // for object deserializers


    open fun updateFilterDefinitions(newFilterDefinitions: List<Filter>) {
        (filterDefinitions as? MutableList)?.let { filterDefinitions ->
            filterDefinitions.clear()

            filterDefinitions.addAll(newFilterDefinitions)
        }
    }


    override fun toString(): String {
        return "$name: ${filterDefinitions.size} filters for $classToFilter"
    }

}