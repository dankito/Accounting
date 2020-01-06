package net.dankito.accounting.javafx.windows.banking

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.control.SplitPane
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.stage.Stage
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.filter.*
import net.dankito.accounting.javafx.controls.VatRateComboBox
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.StyleService
import net.dankito.accounting.javafx.windows.banking.controls.BankAccountTransactionsTable
import net.dankito.accounting.javafx.windows.banking.model.EntityFilterViewModel
import net.dankito.accounting.javafx.windows.banking.model.FilterViewModel
import net.dankito.accounting.javafx.windows.mainwindow.controls.DocumentsTable
import net.dankito.utils.IThreadPool
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
        private const val SaveAndUpdateCreatedDocumentsButtonWidth = 420.0
        private const val ButtonsHorizontalMargin = 12.0

        private val ClassToFilter = BankAccountTransaction::class.java.name
    }


    @Inject
    protected lateinit var presenter: BankAccountsPresenter

    @Inject
    protected lateinit var overviewPresenter: OverviewPresenter

    @Inject
    protected lateinit var styleService: StyleService

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var threadPool: IThreadPool


    private val defaultEntityFilterName = messages["new"]

    private val entityFilters = FXCollections.observableArrayList<EntityFilterViewModel>()

    private var selectedEntityFilter = EntityFilterViewModel()

    private val entityProperties = FXCollections.observableList(AccountTransactionProperty.values().toList())

    private val filterOptions = FXCollections.observableList(FilterOption.stringFilterOptions)


    private var entityFiltersListView: ListView<EntityFilterViewModel> by singleAssign()

    private val entityFilterNameTextField = TextField()

    private var selectedEntityFilterFiltersListView = ListView<FilterViewModel>()

    private var transactionsTable: BankAccountTransactionsTable by singleAssign()

    private val filteredTransactions = FXCollections.observableArrayList<BankAccountTransaction>()

    private val countFilteredTransactions = SimpleStringProperty()

    private val valueAddedTaxRateForCreatedDocumentsComboBox: VatRateComboBox

    private val descriptionForCreatedDocumentsTextField = TextField()

    private val previewCreatedDocumentsForEntityFilter = FXCollections.observableArrayList<Document>()

    private val isSelectedEntityFilterPersisted = SimpleBooleanProperty(false)

    private val hasSelectedEntityFilterUnsavedChanges = SimpleBooleanProperty(false)


    private val subscribedEvent: ISubscribedEvent


    init {
        AppComponent.component.inject(this)

        valueAddedTaxRateForCreatedDocumentsComboBox = VatRateComboBox(null, overviewPresenter.getVatRatesForUser().observable())

        entityFilters.addAll(overviewPresenter.getAccountTransactionsEntityFilters().map { EntityFilterViewModel(it) })
        addNewEntityFilter()

        updateFilteredTransactions(presenter.getAccountTransactions())

        hasSelectedEntityFilterUnsavedChanges.addListener { _, _, _ ->
            entityFiltersListView.refresh()
        }

        subscribedEvent = eventBus.subscribe(BankAccountTransactionsUpdatedEvent::class.java) {
            runLater { updateFilteredTransactions() }
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
        prefWidth = 1300.0

        splitpane {
            setDividerPosition(0, FilterListViewWidth / this@vbox.prefWidth)
            setDividerPosition(1, (FilterListViewWidth + 580.0) / this@vbox.prefWidth)

            vbox {
                SplitPane.setResizableWithParent(this, false)

                anchorpane {
                    label(messages["edit.automatic.account.transaction.import.window.filter.label"]) {
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
                        text = it.name.value + (if (it.item?.isPersisted() == false || it.hasUnsavedChanges.value) "*" else "")
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

                        label(messages["edit.automatic.account.transaction.import.window.define.filter"])

                        label(messages["edit.automatic.account.transaction.import.window.entity.filter.name.label"]) {

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
                        graphic = hbox {
                            prefHeight = 36.0

                            alignment = Pos.CENTER_LEFT

                            removeButton(fontSize = 14.0) {
                                val buttonSize = 26.0
                                minHeight = buttonSize
                                maxHeight = buttonSize
                                minWidth = buttonSize
                                maxWidth = buttonSize

                                action {
                                    selectedEntityFilter.filters.remove(this@cellFormat.item)

                                    updateFilteredTransactionsAndCreatedDocumentsPreview()
                                }

                                hboxConstraints {
                                    marginRight = 12.0
                                }
                            }

                            combobox(it.entityProperty, entityProperties) {
                                prefWidth = 140.0

                                cellFormat {
                                    text = messages["account.transaction.property." + it.name]
                                }

                                selectionModel.selectedItemProperty().addListener { _, _, _ -> filterSettingChanged() }
                            }

                            combobox(it.filterOption, filterOptions) {
                                prefWidth = 130.0

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
                                prefWidth = 120.0

                                textProperty().addListener { _, oldValue, newValue ->

                                    filterSettingChanged()

                                    if (equalsCurrentOrDefaultEntityFilterName(oldValue)) {
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
                })

                borderpane {
                    alignment = Pos.CENTER_LEFT

                    vboxConstraints {
                        marginTop = 12.0
                    }

                    left {
                        label(messages["edit.automatic.account.transaction.import.window.transactions.matching.filter"])
                    }

                    right {
                        hbox {

                            label(countFilteredTransactions) {
                                hboxConstraints {
                                    marginRight = 4.0
                                }
                            }

                            label(messages["edit.automatic.account.transaction.import.window.count.filter.matches"])
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

            vbox {
                form {
                    fieldset(messages["edit.automatic.account.transaction.import.window.created.documents.label"]) {
                        field(messages["value.added.tax.rate"]) {
                            add(valueAddedTaxRateForCreatedDocumentsComboBox.apply {
                                selectionModel.selectedItemProperty().addListener { _, _, _ -> updateCreatedDocumentsPreview() }
                            })
                        }

                        field(messages["description"]) {
                            add(descriptionForCreatedDocumentsTextField.apply {
                                // don't know why but changed text doesn't get applied immediately to selectedEntityFilter.descriptionForCreatedDocuments so i have to use runLater { }
                                textProperty().addListener { _, _, _ -> runLater { updateCreatedDocumentsPreview() } }
                            })
                        }

                        field("") {
                            label(messages["edit.automatic.account.transaction.import.window.format.specifier.explanation"]) {
                                prefHeight = 80.0
                                isWrapText = true
                                tooltip(text)
                            }
                        }
                    }
                }

                label(messages["edit.automatic.account.transaction.import.window.preview.created.documents.label"]) {
                    vboxConstraints {
                        marginBottom = 8.0
                    }
                }

                add(DocumentsTable(previewCreatedDocumentsForEntityFilter, false, overviewPresenter, styleService, threadPool))
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

            button(messages["edit.automatic.account.transaction.import.window.run.filter.once"]) {
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

            button(messages["edit.automatic.account.transaction.import.window.run.filter.after.receiving.transactions"]) {
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

            button(messages["edit.automatic.account.transaction.import.window.save.and.update.created.documents"]) {
                prefWidth = SaveAndUpdateCreatedDocumentsButtonWidth

                visibleWhen(isSelectedEntityFilterPersisted)
                ensureOnlyUsesSpaceIfVisible()

                enableWhen(hasSelectedEntityFilterUnsavedChanges)

                action { updatedPersistedEntityFilterAndDocumentsCreatedByIt() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = RunFilterNowButtonWidth + ButtonsHorizontalMargin
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
            valueAddedTaxRateForCreatedDocumentsComboBox.valueProperty().unbindBidirectional(selectedEntityFilter.valueAddedTaxRateForCreatedDocuments)
            descriptionForCreatedDocumentsTextField.textProperty().unbindBidirectional(selectedEntityFilter.descriptionForCreatedDocuments)

            selectedEntityFilter = entityFilter

            selectedEntityFilterFiltersListView.itemsProperty().bind(entityFilter.filters)

            isSelectedEntityFilterPersisted.value = entityFilter.item.isPersisted()

            entityFilter.reevaluateHasUnsavedChanges()
            hasSelectedEntityFilterUnsavedChanges.bind(entityFilter.hasUnsavedChanges)

            entityFilterNameTextField.textProperty().bindBidirectional(selectedEntityFilter.name)
            valueAddedTaxRateForCreatedDocumentsComboBox.valueProperty().bindBidirectional(entityFilter.valueAddedTaxRateForCreatedDocuments)
            descriptionForCreatedDocumentsTextField.textProperty().bindBidirectional(entityFilter.descriptionForCreatedDocuments)

            updateFilteredTransactionsAndCreatedDocumentsPreview()
        }
    }


    private fun addNewEntityFilter() {
        val newEntityFilterViewModel = EntityFilterViewModel(createNewEntityFilter())

        entityFilters.add(newEntityFilterViewModel)

        selectedEntityFilterChanged(newEntityFilterViewModel)
    }

    private fun createNewEntityFilter(): EntityFilter {
        return EntityFilter(defaultEntityFilterName, BankAccountTransaction::class.java, overviewPresenter.getDefaultVatRateForUser(),
            OverviewPresenter.DefaultDescriptionForCreatedDocuments, listOf(createDefaultFilter()))
    }

    private fun equalsCurrentOrDefaultEntityFilterName(oldValue: String?): Boolean {
        return selectedEntityFilter.name.value == oldValue || selectedEntityFilter.name.value == defaultEntityFilterName
    }


    private fun addFilter() {
        val newFilter = FilterViewModel(mapToAccountTransactionFilter(createDefaultFilter()))

        selectedEntityFilter.filters.add(newFilter)

        updateFilteredTransactionsAndCreatedDocumentsPreview()
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
        updateFilteredTransactionsAndCreatedDocumentsPreview()

        selectedEntityFilter.reevaluateHasUnsavedChanges()
    }

    private fun updateFilteredTransactionsAndCreatedDocumentsPreview() {
        updateFilteredTransactions()

        updateCreatedDocumentsPreview()
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


    private fun updateCreatedDocumentsPreview() {
        val entityFilter = EntityFilter("", "", selectedEntityFilter.valueAddedTaxRateForCreatedDocuments.value,
            selectedEntityFilter.descriptionForCreatedDocuments.value, listOf())

        previewCreatedDocumentsForEntityFilter.setAll(
            filteredTransactions.map { overviewPresenter.mapTransactionToDocument(it, entityFilter) }
        )
    }


    private fun updatedPersistedEntityFilter() {
        val entityFilterViewModel = selectedEntityFilter
        entityFilterViewModel.commit() // TODO: why doesn't it write the changes to it's item?

        val entityFilter = entityFilterViewModel.item

        entityFilter.name = entityFilterViewModel.name.value
        entityFilter.valueAddedTaxRateForCreatedDocuments = entityFilterViewModel.valueAddedTaxRateForCreatedDocuments.value
        entityFilter.descriptionForCreatedDocuments = entityFilterViewModel.descriptionForCreatedDocuments.value

        if (entityFilterViewModel.didFiltersChange) {
            entityFilter.updateFilterDefinitions(mapFiltersFromViewModel())
            // TODO: filters do not get updated in EntityFilterViewModel
        }

        overviewPresenter.saveOrUpdate(entityFilter)

        entityFilterViewModel.reevaluateHasUnsavedChanges()
    }

    private fun updatedPersistedEntityFilterAndDocumentsCreatedByIt() {
        updatedPersistedEntityFilter()

        updatedDocumentsCreatedByEntityFilter()
    }

    private fun updatedDocumentsCreatedByEntityFilter() {
        overviewPresenter.updateDocumentsForEntityFilter(filteredTransactions, selectedEntityFilter.item)
    }


    private fun runFilterNow() {
        transactionsTable.addToExpendituresAndRevenues(filterTransactions())

        close()
    }

    private fun runFilterEachTimeAfterReceivingTransactions() { // TODO: also run filter now
        overviewPresenter.saveOrUpdate(EntityFilter(selectedEntityFilter.name.value, BankAccountTransaction::class.java,
            selectedEntityFilter.valueAddedTaxRateForCreatedDocuments.value,
            selectedEntityFilter.descriptionForCreatedDocuments.value, mapFiltersFromViewModel()))

        close()
    }

}