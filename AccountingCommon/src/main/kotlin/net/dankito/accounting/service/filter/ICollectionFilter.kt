package net.dankito.accounting.service.filter

import net.dankito.accounting.data.model.filter.Filter


interface ICollectionFilter {

    fun <T> filter(filters: List<Filter>, collection: Collection<T>): Collection<T>

}