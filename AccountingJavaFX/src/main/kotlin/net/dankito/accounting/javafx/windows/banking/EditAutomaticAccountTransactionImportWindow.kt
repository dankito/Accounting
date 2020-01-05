package net.dankito.accounting.javafx.windows.banking

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.stage.Stage
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.filter.*
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.controls.BankAccountTransactionsTable
import net.dankito.accounting.javafx.windows.banking.model.EntityFilterViewModel
import net.dankito.accounting.javafx.windows.banking.model.FilterViewModel
import net.dankito.utils.events.IEventBus
import net.dankito.utils.events.ISubscribedEvent
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.removeButton
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*
import javax.inject.Inject


class EditAutomaticAccountTransactionImportWindow : Window() {

    companion object {
        private const val FilterListViewWidth = 120.0

        private const val RunFilterNowButtonWidth = 200.0
        private const val RunFilterEachTimeTransactionsReceivedButtonWidth = 400.0
        private const val ButtonsHorizontalMargin = 12.0

        private val ClassToFilter = BankAccountTransaction::class.java.name
    }


    @Inject
    protected lateinit var presenter: BankAccountsPresenter

    @Inject
    protected lateinit var overviewPresenter: OverviewPresenter

    @Inject
    protected lateinit var eventBus: IEventBus


    private val entityFilters = FXCollections.observableArrayList<EntityFilterViewModel>()

    private var selectedEntityFilter = EntityFilterViewModel(createNewEntityFilter()) // initialize with a dummy value (will be discarded soon)

    private val entityProperties = FXCollections.observableList(AccountTransactionProperty.values().toList())

    private val filterOptions = FXCollections.observableList(FilterOption.stringFilterOptions)


    private var entityFiltersListView: ListView<EntityFilterViewModel> by singleAssign()

    private val entityFilterNameTextField = TextField()

    private var selectedEntityFilterFiltersListView = ListView<FilterViewModel>()

    private var transactionsTable: BankAccountTransactionsTable by singleAssign()

    private val filteredTransactions = FXCollections.observableArrayList<BankAccountTransaction>()

    private val countFilteredTransactions = SimpleStringProperty()

    private val isSelectedEntityFilterPersisted = SimpleBooleanProperty(false)

    private val hasSelectedEntityFilterUnsavedChanges = SimpleBooleanProperty(false)


    private val subscribedEvent: ISubscribedEvent


    init {
        AppComponent.component.inject(this)

        entityFilters.addAll(overviewPresenter.getAccountTransactionsEntityFilters().map { EntityFilterViewModel(it) })
        addNewEntityFilter()

        updateFilteredTransactions(presenter.getAccountTransactions())

        hasSelectedEntityFilterUnsavedChanges.addListener { _, _, _ ->
            entityFiltersListView.refresh()
        }

        subscribedEvent = eventBus.subscribe(BankAccountTransactionsUpdatedEvent::class.java) {
            runLater { updateFilteredTransactions(presenter.getAccountTransactions()) }
        }
    }

    override fun beforeShow(dialogStage: Stage) {
        super.beforeShow(dialogStage)

        dialogStage.setOnCloseRequest {
            subscribedEvent.unsubscribe() // to avoid memory leaks
        }
    }


    override val root = vbox {

        prefHeight = 550.0
        prefWidth = 850.0

        splitpane {
            setDividerPositions(FilterListViewWidth / this@vbox.prefWidth)

            vbox {
                anchorpane {
                    label(messages["edit.automtic.account.transaction.import.window.filter.label"]) {
                        anchorpaneConstraints {
                            topAnchor = 0.0
                            leftAnchor = 0.0
                            bottomAnchor = 0.0
                        }
                    }

                    addButton {
                        action { addNewEntityFilter() }

                        anchorpaneConstraints {
                            topAnchor = 0.0
                            rightAnchor = 0.0
                            bottomAnchor = 0.0
                        }
                    }
                }

                entityFiltersListView = listview(entityFilters) {
                    selectionModel.selectedItemProperty().addListener { _, _, newValue -> selectedEntityFilterChanged(newValue) }

                    selectionModel.select(selectedEntityFilter)

                    isEditable = true

                    cellFormat {
                        text = it.name.value + (if (it.item?.isPersisted() == false || hasUnsavedChanges(it)) "*" else "")
                    }

                    vboxConstraints {
                        vGrow = Priority.ALWAYS
                    }
                }
            }

            vbox {
                anchorpane {
                    hbox {
                        alignment = Pos.CENTER_LEFT

                        label(messages["edit.automtic.account.transaction.import.window.define.filter"])

                        label(messages["edit.automtic.account.transaction.import.window.entity.filter.name.label"]) {

                            hboxConstraints {
                                marginLeft = 24.0
                                marginRight = 6.0
                            }
                        }

                        add(entityFilterNameTextField.apply {
                            prefWidth = 150.0

                            textProperty().addListener { _, _, _ -> entityFiltersListView.refresh() }
                        })


                        anchorpaneConstraints {
                            topAnchor = 0.0
                            leftAnchor = 0.0
                            bottomAnchor = 0.0
                        }
                    }

                    addButton {
                        action { addFilter() }

                        anchorpaneConstraints {
                            topAnchor = 0.0
                            rightAnchor = 0.0
                            bottomAnchor = 0.0
                        }
                    }
                }

                add(selectedEntityFilterFiltersListView.apply {
                    minHeight = 150.0
                    maxHeight = minHeight
                    useMaxWidth = true

                    cellFormat {
                        graphic = borderpane {
                            prefHeight = 36.0

                            left {
                                hbox {
                                    alignment = Pos.CENTER_LEFT

                                    combobox(it.entityProperty, entityProperties) {
                                        cellFormat {
                                            text = messages["account.transaction.property." + it.name]
                                        }

                                        selectionModel.selectedItemProperty().addListener { _, _, _ -> filterSettingChanged() }
                                    }

                                    combobox(it.filterOption, filterOptions) {
                                        cellFormat {
                                            text = messages["filter.option." + it.name]
                                        }

                                        selectionModel.selectedItemProperty().addListener { _, _, _ -> filterSettingChanged() }

                                        hboxConstraints {
                                            marginLeft = 6.0
                                            marginRight = 6.0
                                        }
                                    }

                                    textfield(it.filterText) {
                                        textProperty().addListener { _, oldValue, newValue ->

                                            filterSettingChanged()

                                            if (selectedEntityFilter.name.value == oldValue || selectedEntityFilter.name.value == messages["new"]) {
                                                selectedEntityFilter.name.value = newValue
                                            }
                                        }
                                    }

                                    checkbox(messages["ignore.case"], it.ignoreCase) {
                                        isVisible = it.filterType.value == FilterType.String

                                        ensureOnlyUsesSpaceIfVisible()

                                        selectedProperty().addListener { _, _, _ -> filterSettingChanged() }

                                        hboxConstraints {
                                            marginLeft = 6.0
                                        }
                                    }
                                }
                            }

                            right {
                                removeButton {
                                    action {
                                        selectedEntityFilter.filters.remove(this@cellFormat.item)

                                        updateFilteredTransactions()
                                    }

                                    borderpaneConstraints {
                                        marginLeft = 12.0
                                    }
                                }
                            }
                        }
                    }
                })

                borderpane {
                    alignment = Pos.CENTER_LEFT

                    vboxConstraints {
                        marginTop = 12.0
                    }

                    left {
                        label(messages["edit.automtic.account.transaction.import.window.transactions.matching.filter"])
                    }

                    right {
                        hbox {

                            label(countFilteredTransactions) {
                                hboxConstraints {
                                    marginRight = 4.0
                                }
                            }

                            label(messages["edit.automtic.account.transaction.import.window.count.filter.matches"])
                        }
                    }

                }

                transactionsTable = BankAccountTransactionsTable(presenter, overviewPresenter, filteredTransactions).apply {
                    useMaxWidth = true

                    vboxConstraints {
                        marginTop = 12.0

                        vGrow = Priority.ALWAYS
                    }
                }

                add(transactionsTable)
            }
        }

        anchorpane {
            minHeight = 36.0
            maxHeight = minHeight

            vboxConstraints {
                marginTop = 6.0
            }

            button(messages["close"]) {
                prefWidth = 150.0

                action { close() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    leftAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["edit.automtic.account.transaction.import.window.run.filter.once"]) {
                prefWidth = RunFilterNowButtonWidth

                hiddenWhen(isSelectedEntityFilterPersisted)
                ensureOnlyUsesSpaceIfVisible()

                action { runFilterNow() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = RunFilterEachTimeTransactionsReceivedButtonWidth + ButtonsHorizontalMargin
                    bottomAnchor = 0.0
                }
            }

            button(messages["edit.automtic.account.transaction.import.window.run.filter.after.receiving.transactions"]) {
                prefWidth = RunFilterEachTimeTransactionsReceivedButtonWidth

                hiddenWhen(isSelectedEntityFilterPersisted)
                ensureOnlyUsesSpaceIfVisible()

                action { runFilterEachTimeAfterReceivingTransactions() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["save"]) {
                prefWidth = RunFilterNowButtonWidth

                visibleWhen(isSelectedEntityFilterPersisted)
                ensureOnlyUsesSpaceIfVisible()

                enableWhen(hasSelectedEntityFilterUnsavedChanges)

                action { updatedPersistedEntityFilter() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }
    }


    private fun selectedEntityFilterChanged(entityFilter: EntityFilterViewModel?) {
        entityFilter?.let { // TODO: necessary?
            entityFilterNameTextField.textProperty().unbindBidirectional(selectedEntityFilter.name)

            selectedEntityFilter = entityFilter

            selectedEntityFilterFiltersListView.itemsProperty().bind(entityFilter.filters)

            isSelectedEntityFilterPersisted.value = entityFilter.item.isPersisted()

            entityFilter.reevaluateHasUnsavedChanges()
            hasSelectedEntityFilterUnsavedChanges.bind(entityFilter.hasUnsavedChanges)

            entityFilterNameTextField.textProperty().bindBidirectional(selectedEntityFilter.name)

            updateFilteredTransactions()
        }
    }

    private fun hasUnsavedChanges(entityFilter: EntityFilterViewModel?): Boolean {
        entityFilter?.let {
            return entityFilter.name.isDirty || entityFilter.filters.firstOrNull { it.isDirty } != null
        }

        return false
    }


    private fun addNewEntityFilter() {
        val newEntityFilterViewModel = EntityFilterViewModel(createNewEntityFilter())

        entityFilters.add(newEntityFilterViewModel)

        selectedEntityFilterChanged(newEntityFilterViewModel)
    }

    private fun createNewEntityFilter(): EntityFilter {
        return EntityFilter(messages["new"], BankAccountTransaction::class.java, listOf(createDefaultFilter()))
    }

    private fun addFilter() {
        val newFilter = FilterViewModel(mapToAccountTransactionFilter(createDefaultFilter()))

        selectedEntityFilter.filters.add(newFilter)

        updateFilteredTransactions()
    }

    private fun mapToAccountTransactionFilter(filter: Filter): AccountTransactionFilter {
        return AccountTransactionFilter(filter.type, filter.option, filter.ignoreCase, filter.filterText,
            AccountTransactionProperty.fromPropertyName(filter.propertyToFilter))
    }

    private fun createDefaultFilter(): Filter {
        return Filter(FilterType.String, FilterOption.Contains, true, "",
            BankAccountTransaction::class.java, AccountTransactionProperty.SenderOrReceiverName.propertyName)
    }


    private fun filterSettingChanged() {
        updateFilteredTransactions()

        selectedEntityFilter.reevaluateHasUnsavedChanges()
    }

    private fun updateFilteredTransactions() {
        updateFilteredTransactions(filterTransactions())
    }

    private fun updateFilteredTransactions(filteredTransactions: List<BankAccountTransaction>) {
        this.filteredTransactions.setAll(filteredTransactions.sortedByDescending { it.valueDate })

        countFilteredTransactions.value = filteredTransactions.size.toString()
    }

    private fun filterTransactions(): List<BankAccountTransaction> {
        val filters = mapFiltersFromViewModel()

        return presenter.filterTransactions(filters)
    }

    private fun mapFiltersFromViewModel(): List<Filter> {
        return selectedEntityFilter.filters.map { itemViewModel ->
            Filter(FilterType.String, itemViewModel.filterOption.value, itemViewModel.ignoreCase.value,
                itemViewModel.filterText.value, ClassToFilter, itemViewModel.entityProperty.value.propertyName
            )
        }
    }


    private fun runFilterNow() {
        transactionsTable.addToExpendituresAndRevenues(filterTransactions())

        close()
    }

    private fun updatedPersistedEntityFilter() {
        val entityFilterViewModel = selectedEntityFilter
        entityFilterViewModel.commit() // TODO: why doesn't it write the changes to it's item?

        val entityFilter = entityFilterViewModel.item

        entityFilter.name = entityFilterViewModel.name.value

        if (entityFilterViewModel.didFiltersChange) {
            entityFilter.updateFilterDefinitions(mapFiltersFromViewModel())
            // TODO: filters do not get updated in EntityFilterViewModel
        }

        overviewPresenter.saveOrUpdate(entityFilter)

        entityFilterViewModel.reevaluateHasUnsavedChanges()
    }

    private fun runFilterEachTimeAfterReceivingTransactions() { // TODO: also run filter now
        overviewPresenter.saveOrUpdate(EntityFilter(selectedEntityFilter.name.value, BankAccountTransaction::class.java, mapFiltersFromViewModel()))

        close()
    }

}