package net.dankito.accounting.javafx.windows.banking.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import net.dankito.accounting.data.model.filter.AccountTransactionFilter
import net.dankito.accounting.data.model.filter.AccountTransactionProperty
import net.dankito.accounting.data.model.filter.EntityFilter
import tornadofx.ItemViewModel
import tornadofx.isDirty
import tornadofx.observable


class EntityFilterViewModel(entityFilter: EntityFilter) : ItemViewModel<EntityFilter>(entityFilter) {

    val hasUnsavedChanges = SimpleBooleanProperty(false)

    val name = bind(EntityFilter::name)

    val filters = SimpleListProperty<FilterViewModel>(entityFilter.filterDefinitions.map { FilterViewModel(
        AccountTransactionFilter(it.type, it.option, it.ignoreCase, it.filterText, AccountTransactionProperty.fromPropertyName(it.propertyToFilter))
    ) }.observable())

    val didFiltersChange: Boolean
        get() = filters.size != item.filterDefinitions.size || filters.firstOrNull { it.isDirty } != null


    init {
        name.addListener { _, _, _ -> reevaluateHasUnsavedChanges() }

        filters.addListener { _, _, _ -> reevaluateHasUnsavedChanges() }
    }


    fun reevaluateHasUnsavedChanges() {
        hasUnsavedChanges.value = name.isDirty || didFiltersChange
    }

}