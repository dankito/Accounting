package net.dankito.accounting.data.model.filter


class AccountTransactionFilter(val filterType: FilterType,
                               val filterOption: FilterOption,
                               val ignoreCase: Boolean,
                               val filterText: String,
                               val property: AccountTransactionProperty
) {
}