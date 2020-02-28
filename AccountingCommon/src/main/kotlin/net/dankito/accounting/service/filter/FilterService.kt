package net.dankito.accounting.service.filter

import net.dankito.accounting.data.dao.filter.IEntityFilterDao
import net.dankito.accounting.data.dao.filter.IFilterDao
import net.dankito.accounting.data.model.filter.EntityFilter
import net.dankito.accounting.data.model.filter.Filter


class FilterService(private val collectionFilter: ICollectionFilter,
                    private val entityFilterDao: IEntityFilterDao,
                    private val filterDao: IFilterDao) : IFilterService {


    override fun getAllEntityFilters(): List<EntityFilter> {
        return entityFilterDao.getAll()
    }

    override fun getEntityFiltersForEntity(entityClass: String): List<EntityFilter> {
        return getAllEntityFilters().filter { it.classToFilter == entityClass }
    }


    override fun saveOrUpdate(entityFilter: EntityFilter, updatedFilterDefinitions: List<Filter>?) {
        if (updatedFilterDefinitions != null) {
            entityFilter.filterDefinitions.forEach { filterDefinition ->
                filterDao.delete(filterDefinition)
            }

            entityFilter.updateFilterDefinitions(updatedFilterDefinitions)

            filterDao.saveOrUpdate(entityFilter.filterDefinitions)
        }

        entityFilterDao.saveOrUpdate(entityFilter)
    }

    override fun delete(entityFilter: EntityFilter) {
        entityFilter.filterDefinitions.forEach { filter ->
            filterDao.delete(filter)
        }

        entityFilterDao.delete(entityFilter)
    }


    override fun <T> filterCollection(entityFilter: EntityFilter, collection: Collection<T>): Collection<T> {
        return collectionFilter.filter(entityFilter.filterDefinitions, collection)
    }

}