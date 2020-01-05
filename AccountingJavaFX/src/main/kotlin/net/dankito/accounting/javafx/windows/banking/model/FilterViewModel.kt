package net.dankito.accounting.javafx.windows.banking.model

import net.dankito.accounting.data.model.filter.AccountTransactionFilter
import tornadofx.ItemViewModel


class FilterViewModel(initialValue: AccountTransactionFilter? = null)
    : ItemViewModel<AccountTransactionFilter>(initialValue) {


    val entityProperty = bind(AccountTransactionFilter::property)

    val filterType = bind(AccountTransactionFilter::filterType)

    val filterOption = bind(AccountTransactionFilter::filterOption)

    val filterText = bind(AccountTransactionFilter::filterText)

    val ignoreCase = bind(AccountTransactionFilter::ignoreCase)


    override fun toString(): String {
        return "${entityProperty.value} ${filterOption.value} ${filterText.value}"
    }

}