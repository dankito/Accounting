package net.dankito.accounting.service.filter

import net.dankito.accounting.data.model.filter.EntityFilter


interface IFilterService {

    fun getAllEntityFilters(): List<EntityFilter>

    fun getEntityFiltersForEntity(entityClass: String): List<EntityFilter>


    fun saveOrUpdate(entityFilter: EntityFilter)

    fun delete(entityFilter: EntityFilter)


    fun <T> filterCollection(entityFilter: EntityFilter, collection: Collection<T>): Collection<T>

}