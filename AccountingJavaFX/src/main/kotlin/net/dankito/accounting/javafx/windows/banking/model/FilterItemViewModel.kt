package net.dankito.accounting.javafx.windows.banking.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.accounting.data.model.filter.AccountTransactionFilter
import net.dankito.accounting.data.model.filter.AccountTransactionProperty
import net.dankito.accounting.data.model.filter.FilterOption
import tornadofx.ItemViewModel


class FilterItemViewModel(initialValue: AccountTransactionFilter? = null)
    : ItemViewModel<AccountTransactionFilter>(initialValue) {


    val entityProperty = SimpleObjectProperty<AccountTransactionProperty>(item?.property)

    val filterOption = SimpleObjectProperty<FilterOption>(item?.filterOption)

    val ignoreCase = SimpleBooleanProperty(item?.ignoreCase ?: true)

    val filterText = SimpleStringProperty("")

}