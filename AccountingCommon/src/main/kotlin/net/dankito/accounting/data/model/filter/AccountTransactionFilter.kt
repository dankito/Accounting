package net.dankito.accounting.data.model.filter


class AccountTransactionFilter(val filterType: FilterType, // TODO: currently not needed
                               val filterOption: StringFilterOption, // TODO: make generic
                               val ignoreCase: Boolean,
                               val property: AccountTransactionProperty // TODO: make generic
) {
}