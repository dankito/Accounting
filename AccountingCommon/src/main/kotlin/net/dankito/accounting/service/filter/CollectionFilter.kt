package net.dankito.accounting.service.filter

import net.dankito.accounting.data.model.filter.Filter
import net.dankito.accounting.data.model.filter.FilterOption
import net.dankito.accounting.data.model.filter.FilterType
import org.slf4j.LoggerFactory


open class CollectionFilter : ICollectionFilter {

    companion object {
        private val log = LoggerFactory.getLogger(CollectionFilter::class.java)
    }


    override fun <T> filter(
        filters: List<Filter>,
        collection: Collection<T>
    ): Collection<T> {
        if (filters.isEmpty()) {
            return collection
        }

        return collection.filter { item ->
            filters.forEach { filter ->
                if (doesFilterMatch(item, filter) == false) {
                    return@filter false
                }
            }

            return@filter true
        }
    }

    protected open fun <T> doesFilterMatch(item: T, filter: Filter): Boolean {
        val value = extractValue(item, filter)

        if (filter.type == FilterType.String) {
            (value as? String)?.let { stringValue ->
                return doesStringFilterMatch(stringValue, filter)
            }
            ?: log.error("Filter is of type String, but extracted value '$value' is of type ${value?.javaClass}")
        }

        return false
    }

    protected open fun <T> extractValue(item: T, filter: Filter): Any? {
        try {
            val type = Class.forName(filter.classToFilter)
            val field = type.getDeclaredField(filter.propertyToFilter)

            field.isAccessible = true

            return field.get(item)
        } catch (e: Exception) {
            log.error("Could not extract value of property ${filter.propertyToFilter} from $item", e)
        }

        return null
    }

    protected open fun doesStringFilterMatch(value: String, filter: Filter): Boolean {
        val ignoreCase = filter.ignoreCase
        val filterText = filter.filterText

        return when (filter.option) {
            FilterOption.Equals -> value.equals(filterText, ignoreCase)
            FilterOption.EqualsNot -> value.equals(filterText, ignoreCase) == false
            FilterOption.Contains -> value.contains(filterText, ignoreCase)
            FilterOption.ContainsNot -> filterText.isEmpty() || value.contains(filterText, ignoreCase) == false
            FilterOption.StartsWith -> value.startsWith(filterText, ignoreCase)
            FilterOption.EndsWith -> value.endsWith(filterText, ignoreCase)
        }
    }

}