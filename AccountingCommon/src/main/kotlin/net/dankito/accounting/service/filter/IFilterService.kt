package net.dankito.accounting.service.filter

import net.dankito.accounting.data.model.filter.EntityFilter
import net.dankito.accounting.data.model.filter.Filter


interface IFilterService {

    fun getAllEntityFilters(): List<EntityFilter>

    fun getEntityFiltersForEntity(entityClass: String): List<EntityFilter>


    fun saveOrUpdate(entityFilter: EntityFilter, updatedFilterDefinitions: List<Filter>?)

    fun delete(entityFilter: EntityFilter)


    fun <T> filterCollection(entityFilter: EntityFilter, collection: Collection<T>): Collection<T>

}