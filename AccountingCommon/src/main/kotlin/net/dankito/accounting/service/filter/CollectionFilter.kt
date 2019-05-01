package net.dankito.accounting.service.filter

import net.dankito.accounting.data.model.filter.StringFilter
import net.dankito.accounting.data.model.filter.StringFilterOption


open class CollectionFilter : ICollectionFilter {

    override fun <T> filterStringField(collection: Collection<T>, filters: List<StringFilter<T>>): Collection<T> {
        if (filters.isEmpty()) {
            return collection
        }

        return collection.filter { item ->
            filters.forEach { filter ->
                if (doesStringFilterMatch(item, filter) == false) {
                    return@filter false
                }
            }

            return@filter true
        }
    }

    protected open fun <T> doesStringFilterMatch(item: T, filter: StringFilter<T>): Boolean {
        val ignoreCase = filter.ignoreCase
        val filterText = filter.filterText

        val value = filter.valueExtractor(item)

        return when (filter.filterOption) {
            StringFilterOption.Equals -> value.equals(filterText, ignoreCase)
            StringFilterOption.EqualsNot -> value.equals(filterText, ignoreCase) == false
            StringFilterOption.Contains -> value.contains(filterText, ignoreCase)
            StringFilterOption.ContainsNot -> value.contains(filterText, ignoreCase) == false
            StringFilterOption.StartsWith -> value.startsWith(filterText, ignoreCase)
            StringFilterOption.EndsWith -> value.endsWith(filterText, ignoreCase)
        }
    }

}