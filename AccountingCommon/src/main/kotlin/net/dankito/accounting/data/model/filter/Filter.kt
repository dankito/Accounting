package net.dankito.accounting.data.model.filter

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Enumerated


@Entity
class Filter(

    @Enumerated
    @Column
    val type: FilterType,

    @Enumerated
    @Column
    val option: FilterOption,

    /**
     * Only relevant for [FilterType.String]
     */
    @Column
    val ignoreCase: Boolean,

    @Column
    val filterText: String,

    @Column
    val classToFilter: String,

    @Column
    val propertyToFilter: String

) : BaseEntity() {


    constructor(type: FilterType, option: FilterOption, ignoreCase: Boolean, filterText: String,
                classToFilter: Class<*>, propertyToFilter: String)
            : this(type, option, ignoreCase, filterText, classToFilter.name, propertyToFilter)

    internal constructor() : this(FilterType.String, FilterOption.Contains, true, "", "", "") // for object deserializers


    override fun toString(): String {
        return "Filter $classToFilter.$propertyToFilter $option $filterText"
    }

}