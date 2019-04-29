package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.beans.binding.ObjectBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Callback
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.utils.javafx.ui.controls.searchtextfield
import tornadofx.*
import java.text.DateFormat
import javax.inject.Inject


class BankAccountsTab : View() {

    companion object {

        private val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    }


    @Inject
    lateinit var presenter: BankAccountsPresenter

    @Inject
    lateinit var overviewPresenter: OverviewPresenter


    private val allTransactions = FXCollections.observableArrayList<BankAccountTransaction>()

    private val transactionsToDisplay = FXCollections.observableArrayList<BankAccountTransaction>()


    private val searchText = SimpleStringProperty("")

    private val balance = SimpleStringProperty("")

    private val isUpdatingTransactions = SimpleBooleanProperty(false)


    init {
        AppComponent.component.inject(this)

        searchText.addListener { _, _, newValue -> searchEntries(newValue) }

        retrievedAccountTransactions(presenter.getAccountTransactions())
    }


    override val root = vbox {
        borderpane {
            minHeight = 36.0
            maxHeight = 36.0

            left = label(messages["main.window.tab.bank.accounts.search.label"]) {
                borderpaneConstraints {
                    alignment = Pos.CENTER_LEFT
                    margin = Insets(4.0, 12.0, 4.0, 4.0)
                }
            }

            center {
                searchtextfield(searchText) {

                }
            }

            right = hbox {
                alignment = Pos.CENTER_LEFT

                label(messages["main.window.tab.bank.accounts.balance.label"]) {
                    hboxConstraints {
                        alignment = Pos.CENTER_LEFT
                        marginLeft = 48.0
                        marginRight = 12.0
                    }
                }

                label(balance) {
                    minWidth = 50.0
                    alignment = Pos.CENTER_RIGHT
                }

                 button(messages["update..."]) { // TODO: set icon
                    disableWhen(isUpdatingTransactions)

                    action { updateAccountTransactions() }

                    hboxConstraints {
                        marginLeft = 12.0
                        marginRight = 4.0
                    }
                }
            }
        }

        tableview<BankAccountTransaction>(transactionsToDisplay) {
            column(messages["main.window.tab.bank.accounts.column.header.value.date"], BankAccountTransaction::valueDate) {
                prefWidth = 150.0

                cellFormat {
                    text = ValueDateFormat.format(it)
                    alignment = Pos.CENTER_LEFT
                    paddingLeft = 4.0
                }
            }

            val usageColumn = TableColumn<BankAccountTransaction, BankAccountTransaction>(messages["main.window.tab.bank.accounts.column.header.usage"])
            usageColumn.cellFragment(UsageCellFragment::class)
            usageColumn.cellValueFactory = Callback { object : ObjectBinding<BankAccountTransaction>() {
                override fun computeValue(): BankAccountTransaction {
                    return it.value
                }

            } }
            usageColumn.weightedWidth(4.0)
            columns.add(usageColumn)

            column(messages["main.window.tab.bank.accounts.column.header.amount"], BankAccountTransaction::amount) {
                prefWidth = 100.0

                cellFormat {
                    text = overviewPresenter.getCurrencyString(it)
                    alignment = Pos.CENTER_RIGHT
                    paddingRight = 4.0

                    style {
                        textFill = if (it.toLong() < 0)  Color.RED else Color.GREEN
                    }
                }


                columnResizePolicy = SmartResize.POLICY

                vgrow = Priority.ALWAYS

                setOnMouseClicked { tableClicked(it, this@tableview.selectionModel.selectedItem) }
            }
        }
    }


    private fun searchEntries() {
        searchEntries(searchText.value)
    }

    private fun searchEntries(query: String) {
        if(query.isEmpty()) {
            transactionsToDisplay.setAll(allTransactions)
        }
        else {
            transactionsToDisplay.setAll(getSearchEntriesResult(query))
        }
    }

    private fun getSearchEntriesResult(query: String): List<BankAccountTransaction> {
        val result = mutableListOf<BankAccountTransaction>()
        val lowerCaseQuery = query.toLowerCase()

        allTransactions.forEach { entry ->
            if (entry.usage.toLowerCase().contains(lowerCaseQuery)
                || entry.senderOrReceiverName.toLowerCase().contains(lowerCaseQuery)
                || ValueDateFormat.format(entry.valueDate).contains(lowerCaseQuery)
                || entry.amount.toString().contains(lowerCaseQuery)) {

                result.add(entry)
            }
        }

        return result
    }


    private fun updateAccountTransactions() {
        isUpdatingTransactions.value = true

        presenter.updateAccountTransactionsAsync { transactions ->
            runLater {
                retrievedAccountTransactions(transactions)
            }
        }
    }

    private fun retrievedAccountTransactions(transactions: List<BankAccountTransaction>) {
        allTransactions.setAll(transactions.sortedByDescending { it.valueDate })

        searchEntries()

        if (allTransactions.isNotEmpty()) { // latest transaction holds current balance // TODO: this isn't correct under all circumstances, e.g. if there is more than one transaction at this day
            balance.value = overviewPresenter.getCurrencyString(allTransactions[0].balance)
        }

        isUpdatingTransactions.value = false
    }

    private fun tableClicked(event: MouseEvent, selectedItem: BankAccountTransaction?) {
        if(event.clickCount == 2 && event.button == MouseButton.PRIMARY) {
            if(selectedItem != null) {
                presenter.showTransactionDetailsWindow(selectedItem)
            }
        }
    }

}