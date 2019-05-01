package net.dankito.accounting.data.model.filter


class StringFilter<T>(val filterOption: StringFilterOption, val filterText: String, val ignoreCase: Boolean = true,
                      val valueExtractor: (T) -> String) {

    override fun toString(): String {
        return "$filterOption $filterText"
    }

}