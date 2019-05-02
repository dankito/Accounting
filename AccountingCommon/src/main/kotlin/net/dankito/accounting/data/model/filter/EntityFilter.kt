package net.dankito.accounting.data.model.filter

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
class EntityFilter(

    @Column
    val classToFilter: String,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val filterDefinitions: List<Filter>

) : BaseEntity() {


    constructor(classToFilter: Class<*>, filterDefinitions: List<Filter>) : this(classToFilter.name, filterDefinitions)

    internal constructor() : this("", listOf()) // for object deserializers


    override fun toString(): String {
        return "${filterDefinitions.size} filters for $classToFilter"
    }

}