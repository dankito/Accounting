package net.dankito.accounting.data.model.filter


enum class FilterOption(val supportedTypes: List<FilterType>) {

    Equals(listOf(FilterType.String, FilterType.Int, FilterType.Date)),

    EqualsNot(listOf(FilterType.String, FilterType.Int, FilterType.Date)),

    Contains(listOf(FilterType.String)),

    ContainsNot(listOf(FilterType.String)),

    StartsWith(listOf(FilterType.String)),

    EndsWith(listOf(FilterType.String));


    companion object {

        val stringFilterOptions: List<FilterOption>
            get() = getFilterOptionsForType(FilterType.String)


        private fun getFilterOptionsForType(type: FilterType): List<FilterOption> {
            return FilterOption.values().filter { it.supportedTypes.contains(type) }
        }

    }

}