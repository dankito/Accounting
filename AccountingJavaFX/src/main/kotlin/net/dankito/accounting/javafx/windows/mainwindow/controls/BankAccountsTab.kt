package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.event.UpdatingBankAccountTransactionsEvent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.controls.BankAccountTransactionsTable
import net.dankito.utils.events.IEventBus
import net.dankito.utils.javafx.ui.controls.UpdateButton
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.searchtextfield
import net.dankito.utils.javafx.ui.controls.updateButton
import tornadofx.*
import java.text.DateFormat
import javax.inject.Inject


class BankAccountsTab : View() {

    companion object {

        private val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    }


    @Inject
    protected lateinit var presenter: BankAccountsPresenter

    @Inject
    protected lateinit var overviewPresenter: OverviewPresenter

    @Inject
    protected lateinit var eventBus: IEventBus


    private val allTransactions = FXCollections.observableArrayList<BankAccountTransaction>()

    private val transactionsToDisplay = FXCollections.observableArrayList<BankAccountTransaction>()


    private val searchText = SimpleStringProperty("")

    private val balance = SimpleStringProperty("")

    private val updateButton: UpdateButton = updateButton(messages["update..."], { updateAccountTransactions() })


    init {
        AppComponent.component.inject(this)

        searchText.addListener { _, _, newValue -> searchEntries(newValue) }

        retrievedAccountTransactions(presenter.getAccountTransactions())

        eventBus.subscribe(UpdatingBankAccountTransactionsEvent::class.java) {
            runLater { updateButton.setIsUpdating() }
        }

        eventBus.subscribe(BankAccountTransactionsUpdatedEvent::class.java) {
            runLater { retrievedAccountTransactions(presenter.getAccountTransactions()) }
        }
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
                        marginRight = 6.0
                    }
                }

                label(balance) {
                    minWidth = 50.0
                    alignment = Pos.CENTER_RIGHT
                }

                add(updateButton.apply {

                    hboxConstraints {
                        marginLeft = 12.0
                    }
                })

                label(messages["main.window.tab.bank.accounts.automatically.create.documents.label"]) {

                    hboxConstraints {
                        marginLeft = 18.0
                        marginRight = 6.0
                    }
                }

                addButton {
                    action { presenter.showEditAutomaticAccountTransactionImportWindow() }
                }
            }
        }

        add(BankAccountTransactionsTable(presenter, overviewPresenter, transactionsToDisplay))

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
        presenter.updateAccountsTransactionsAsync { transactions ->
            runLater {
                retrievedAccountTransactions(transactions)
            }
        }
    }

    private fun retrievedAccountTransactions(transactions: List<BankAccountTransaction>) {
        allTransactions.setAll(transactions.sortedByDescending { it.valueDate })

        searchEntries()

        if (allTransactions.isNotEmpty()) { // latest transaction holds current balance // TODO: this isn't correct under all circumstances, e.g. if there is more than one transaction at this day
            balance.value = overviewPresenter.getCurrencyString(allTransactions[0].account.balance)
        }

        updateButton.resetIsUpdating()
    }

}