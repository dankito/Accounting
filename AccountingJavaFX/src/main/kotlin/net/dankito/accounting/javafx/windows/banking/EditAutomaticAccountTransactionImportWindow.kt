package net.dankito.accounting.javafx.windows.banking

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.layout.Priority
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.controls.BankAccountTransactionsTable
import net.dankito.accounting.javafx.windows.banking.controls.StringFilterField
import net.dankito.accounting.javafx.windows.banking.model.StringFilterOption
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*
import javax.inject.Inject


class EditAutomaticAccountTransactionImportWindow : Window() {


    @Inject
    lateinit var presenter: BankAccountsPresenter

    @Inject
    lateinit var overviewPresenter: OverviewPresenter


    private val filterOtherName = SimpleBooleanProperty(true)
    private val otherNameEnteredFilter = SimpleStringProperty()
    private val otherNameFilterOption = SimpleObjectProperty<StringFilterOption>(StringFilterOption.Contains)
    private val otherNameIgnoreCase = SimpleBooleanProperty(true)

    private val filterUsage = SimpleBooleanProperty(false)
    private val usageEnteredFilter = SimpleStringProperty()
    private val usageFilterOption = SimpleObjectProperty<StringFilterOption>(StringFilterOption.Contains)
    private val usageIgnoreCase = SimpleBooleanProperty(true)


    private val allTransactions: List<BankAccountTransaction>

    private val filteredTransactions = FXCollections.observableArrayList<BankAccountTransaction>()

    private val countFilteredTransactions = SimpleStringProperty()


    init {
        AppComponent.component.inject(this)

        // TODO: add event bus listener to get informed when transactions get updated
        allTransactions = presenter.getAccountTransactions()
        updateFilteredTransactions(allTransactions)

        initFilterOptions()
    }


    override val root = vbox {
        prefHeight = 550.0
        prefWidth = 800.0

        paddingAll = 4.0


        add(StringFilterField("edit.automtic.account.transaction.import.window.other.name", filterOtherName,
            otherNameEnteredFilter, otherNameFilterOption, otherNameIgnoreCase).apply {

        })

        add(StringFilterField("edit.automtic.account.transaction.import.window.usage", filterUsage,
            usageEnteredFilter, usageFilterOption, usageIgnoreCase).apply {

            root.vboxConstraints {
                marginTop = 12.0
            }
        })

        borderpane {
            right {
                hbox {

                    label(countFilteredTransactions)

                    label(messages["edit.automtic.account.transaction.import.window.count.filter.results"]) {
                        borderpaneConstraints {
                            marginLeft = 3.0
                        }
                    }
                }
            }
        }

        add(BankAccountTransactionsTable(overviewPresenter, filteredTransactions).apply {
            useMaxWidth = true

            vboxConstraints {
                marginTop = 12.0

                vGrow = Priority.ALWAYS
            }
        })

    }


    private fun initFilterOptions() {
        filterOtherName.addListener { _, _, _ -> updateFilteredTransactions() }
        otherNameEnteredFilter.addListener { _, _, _ -> updateFilteredTransactions() }
        otherNameFilterOption.addListener { _, _, _ -> updateFilteredTransactions() }
        otherNameIgnoreCase.addListener { _, _, _ -> updateFilteredTransactions() }
    }


    private fun updateFilteredTransactions() {
        updateFilteredTransactions(filterTransactions())
    }

    private fun updateFilteredTransactions(filteredTransactions: List<BankAccountTransaction>) {
        this.filteredTransactions.setAll(filteredTransactions.sortedByDescending { it.valueDate })

        countFilteredTransactions.value = filteredTransactions.size.toString()
    }

    private fun filterTransactions(): List<BankAccountTransaction> {
        if (filterOtherName.value) {
            val otherNameIgnoreCase = this.otherNameIgnoreCase.value
            val otherNameFilterText = this.otherNameEnteredFilter.value

            return allTransactions.filter { transaction ->
                when (otherNameFilterOption.value) {
                    StringFilterOption.Is -> transaction.senderOrReceiverName.equals(otherNameFilterText, otherNameIgnoreCase)
                    StringFilterOption.IsNot -> transaction.senderOrReceiverName.equals(otherNameFilterText, otherNameIgnoreCase) == false
                    StringFilterOption.Contains -> transaction.senderOrReceiverName.contains(otherNameFilterText, otherNameIgnoreCase)
                    StringFilterOption.ContainsNot -> transaction.senderOrReceiverName.contains(otherNameFilterText, otherNameIgnoreCase) == false
                    StringFilterOption.StartsWith -> transaction.senderOrReceiverName.startsWith(otherNameFilterText, otherNameIgnoreCase)
                    StringFilterOption.EndsWith -> transaction.senderOrReceiverName.endsWith(otherNameFilterText, otherNameIgnoreCase)
                }
            }
        }
        else {
            return allTransactions
        }
    }

}