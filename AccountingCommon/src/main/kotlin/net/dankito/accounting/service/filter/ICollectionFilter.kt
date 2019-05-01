package net.dankito.accounting.service.filter

import net.dankito.accounting.data.model.filter.StringFilter


interface ICollectionFilter {

    fun <T> filterStringField(collection: Collection<T>, filters: List<StringFilter<T>>): Collection<T>

}