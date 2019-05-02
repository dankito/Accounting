package net.dankito.accounting.javafx.windows.banking

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.filter.*
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.controls.BankAccountTransactionsTable
import net.dankito.accounting.javafx.windows.banking.model.FilterItemViewModel
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.removeButton
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*
import javax.inject.Inject


class EditAutomaticAccountTransactionImportWindow : Window() {

    companion object {
        private const val ButtonsWidth = 150.0
        private const val ButtonsHorizontalMargin = 12.0

        private val ClassToFilter = BankAccountTransaction::class.java.name
    }


    @Inject
    lateinit var presenter: BankAccountsPresenter

    @Inject
    lateinit var overviewPresenter: OverviewPresenter


    private val appliedFilters = FXCollections.observableArrayList<AccountTransactionFilter>(createDefaultFilter())

    private val entityProperties = FXCollections.observableList(AccountTransactionProperty.values().toList())

    private val filterOptions = FXCollections.observableList(FilterOption.stringFilterOptions)

    private val filterItemViewModels = mutableListOf<FilterItemViewModel>()


    private var transactionsTable: BankAccountTransactionsTable by singleAssign()

    private val filteredTransactions = FXCollections.observableArrayList<BankAccountTransaction>()

    private val countFilteredTransactions = SimpleStringProperty()


    init {
        AppComponent.component.inject(this)

        // TODO: add event bus listener to get informed when transactions get updated
        updateFilteredTransactions(presenter.getAccountTransactions())
    }


    override val root = vbox {
        prefHeight = 550.0
        prefWidth = 800.0

        paddingAll = 4.0


        borderpane {
            alignment = Pos.CENTER_LEFT

            left {
                label(messages["edit.automtic.account.transaction.import.window.define.filter"])
            }

            right {
                addButton {
                    action {
                        appliedFilters.add(createDefaultFilter())
                        updateFilteredTransactions()
                    }
                }
            }
        }

        listview(appliedFilters) {
            minHeight = 150.0
            maxHeight = minHeight
            useMaxWidth = true

            cellFormat {
                val itemViewModel = createFilterItemViewModel(it)

                graphic = borderpane {
                    prefHeight = 36.0

                    left {
                        hbox {
                            alignment = Pos.CENTER_LEFT

                            combobox(itemViewModel.entityProperty, entityProperties) {
                                cellFormat {
                                    text = messages["account.transaction.property." + it.name]
                                }
                            }

                            combobox(itemViewModel.filterOption, filterOptions) {
                                cellFormat {
                                    text = messages["filter.option." + it.name]
                                }

                                hboxConstraints {
                                    marginLeft = 6.0
                                    marginRight = 6.0
                                }
                            }

                            textfield(itemViewModel.filterText)

                            checkbox(messages["ignore.case"], itemViewModel.ignoreCase) {
                                isVisible = it.filterType == FilterType.String

                                ensureOnlyUsesSpaceIfVisible()

                                hboxConstraints {
                                    marginLeft = 6.0
                                }
                            }
                        }
                    }

                    right {
                        removeButton {
                            action {
                                appliedFilters.remove(this@cellFormat.item)
                                filterItemViewModels.removeIf { it.item == this@cellFormat.item }
                                updateFilteredTransactions()
                            }

                            borderpaneConstraints {
                                marginLeft = 12.0
                            }
                        }
                    }
                }
            }
        }


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

        anchorpane {
            minHeight = 36.0
            maxHeight = minHeight

            vboxConstraints {
                marginTop = 6.0
            }

            button(messages["cancel"]) {
                prefWidth = ButtonsWidth

                action { close() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 2 * (ButtonsWidth + ButtonsHorizontalMargin)
                    bottomAnchor = 0.0
                }
            }

            button(messages["edit.automtic.account.transaction.import.window.apply.filter.now"]) {
                prefWidth = ButtonsWidth

                action { runFilterNow() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = ButtonsWidth + ButtonsHorizontalMargin
                    bottomAnchor = 0.0
                }
            }

            button(messages["edit.automtic.account.transaction.import.window.run.filter.after.receiving.transactions"]) {
                prefWidth = ButtonsWidth

                action { runFilterEachTimeAfterReceivingTransactions() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

    }


    private fun createDefaultFilter(): AccountTransactionFilter {
        return AccountTransactionFilter(FilterType.String, FilterOption.Contains, true,
            AccountTransactionProperty.SenderOrReceiverName)
    }

    private fun createFilterItemViewModel(filter: AccountTransactionFilter): FilterItemViewModel {
        filterItemViewModels.firstOrNull { it.item == filter }?.let {
            return it
        }

        val itemViewModel = FilterItemViewModel(filter)

        filterItemViewModels.add(itemViewModel)

        itemViewModel.entityProperty.addListener { _, _, _ -> updateFilteredTransactions() }
        itemViewModel.filterOption.addListener { _, _, _ -> updateFilteredTransactions() }
        itemViewModel.ignoreCase.addListener { _, _, _ -> updateFilteredTransactions() }
        itemViewModel.filterText.addListener { _, _, _ -> updateFilteredTransactions() }

        return itemViewModel
    }


    private fun updateFilteredTransactions() {
        updateFilteredTransactions(filterTransactions())
    }

    private fun updateFilteredTransactions(filteredTransactions: List<BankAccountTransaction>) {
        this.filteredTransactions.setAll(filteredTransactions.sortedByDescending { it.valueDate })

        countFilteredTransactions.value = filteredTransactions.size.toString()
    }

    private fun filterTransactions(): List<BankAccountTransaction> {
        val filters = createFilters()

        return presenter.filterTransactions(filters)
    }

    private fun createFilters(): List<Filter> {
        return filterItemViewModels.map { itemViewModel ->
            Filter(FilterType.String, itemViewModel.filterOption.value, itemViewModel.ignoreCase.value,
                itemViewModel.filterText.value, ClassToFilter, itemViewModel.entityProperty.value.propertyName
            )
        }
    }


    private fun runFilterNow() {
        transactionsTable.addToExpendituresAndRevenues(filterTransactions())

        close()
    }

    private fun runFilterEachTimeAfterReceivingTransactions() {
        overviewPresenter.saveOrUpdate(EntityFilter(BankAccountTransaction::class.java, createFilters()))

        close()
    }

}