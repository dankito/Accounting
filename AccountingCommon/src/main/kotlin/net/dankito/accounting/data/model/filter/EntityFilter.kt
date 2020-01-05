package net.dankito.accounting.data.model.filter

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
class EntityFilter(

    @Column
    var name: String,

    @Column
    val classToFilter: String,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val filterDefinitions: List<Filter>

) : BaseEntity() {


    constructor(name: String, classToFilter: Class<*>, filterDefinitions: List<Filter>) : this(name, classToFilter.name, filterDefinitions)

    internal constructor() : this("", "", listOf()) // for object deserializers


    fun updateFilterDefinitions(newFilterDefinitions: List<Filter>) {
        (filterDefinitions as? MutableList)?.let { filterDefinitions ->
            filterDefinitions.clear()

            filterDefinitions.addAll(newFilterDefinitions)
        }
    }


    override fun toString(): String {
        return "$name: ${filterDefinitions.size} filters for $classToFilter"
    }

}