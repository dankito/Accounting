package net.dankito.accounting.javafx.windows.banking.controls

import javafx.beans.binding.ObjectBinding
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.input.ContextMenuEvent
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Stop
import javafx.util.Callback
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.banking.model.ExpenditureRevenueType
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.DateFormat


class BankAccountTransactionsTable(private val presenter: BankAccountsPresenter,
                                   private val overviewPresenter: OverviewPresenter,
                                   transactionsToDisplay: ObservableList<BankAccountTransaction>
) : TableView<BankAccountTransaction>(transactionsToDisplay) {


    companion object {

        private val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

        private val BackgroundStops = listOf(Stop(0.0, Color.WHITE), Stop(1.0, Color.web("#21b121")))
        private val BackgroudIfAlreadyCreatedDocumentFromTransaction = Background(BackgroundFill(
            LinearGradient(0.0, 0.0, 0.0, 1.0, true, CycleMethod.NO_CYCLE, BackgroundStops), CornerRadii.EMPTY, Insets.EMPTY))

    }


    private var currentMenu: ContextMenu? = null


    init {
        initUi()
    }


    private fun initUi() {
        column(messages["main.window.tab.bank.accounts.column.header.value.date"], BankAccountTransaction::valueDate) {
            prefWidth = 150.0

            cellFormat {
                text = ValueDateFormat.format(it)
                alignment = Pos.CENTER_LEFT
                paddingLeft = 4.0
            }
        }

        val usageColumn = TableColumn<BankAccountTransaction, BankAccountTransaction>(messages["main.window.tab.bank.accounts.column.header.usage"])
        usageColumn.cellFragment(DefaultScope, UsageCellFragment::class)
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
        }


        setRowFactory { object : TableRow<BankAccountTransaction>() {

            private var cellDefaultBackground: Background? = null

            override fun updateItem(item: BankAccountTransaction?, empty: Boolean) {
                super.updateItem(item, empty)

                item?.createdDocument?.let { document ->
                    cellDefaultBackground = this.background // backup background to restore it when row gets reused for other transaction
                    this.background = BackgroudIfAlreadyCreatedDocumentFromTransaction
                    this.tooltip = createTooltipForCreatedDocument(document)
                }
                ?: run {
                    this.tooltip = null
                    cellDefaultBackground?.let { this.background = cellDefaultBackground }
                }
            }

        }}


        selectionModel.selectionMode = SelectionMode.MULTIPLE

        setOnMouseClicked { tableClicked(it, this.selectionModel.selectedItem) }

        setOnContextMenuRequested { event -> showContextMenu(event, this.selectionModel.selectedItems) }
    }


    private fun tableClicked(event: MouseEvent, selectedItem: BankAccountTransaction?) {
        if (event.button == MouseButton.PRIMARY || event.button == MouseButton.MIDDLE) {
            currentMenu?.hide()
        }

        if(event.clickCount == 2 && event.button == MouseButton.PRIMARY) {
            if(selectedItem != null) {
                presenter.showTransactionDetailsWindow(selectedItem)
            }
        }
    }

    private fun showContextMenu(event: ContextMenuEvent, selectedItems: List<BankAccountTransaction>) {
        currentMenu?.hide()

        currentMenu = createContextMenuForItems(selectedItems)
        currentMenu?.show(this, event.screenX, event.screenY)
    }

    private fun createContextMenuForItems(selectedItems: List<BankAccountTransaction>): ContextMenu? {
        val contextMenu = ContextMenu()
        val type = getExpenditureRevenueType(selectedItems)
        val showEditDocumentMenuItem = selectedItems.find { it.createdDocument != null } != null


        contextMenu.apply {
            item(messages[getResourceKeyForDirectlyAddingToExpendituresOrRevenues(type)]) {
                action {
                    addToExpendituresAndRevenues(selectedItems)
                }
            }

            item(messages[getResourceKeyForAdjustBeforeAddingToExpendituresOrRevenues(type)]) {
                action {
                    adjustBeforeAddingToExpendituresAndRevenues(selectedItems)
                }
            }

            if (showEditDocumentMenuItem) {
                separator()

                item(messages["bank.account.transactions.table.context.menu.edit.document"]) {
                    action { showCreatedDocuments(selectedItems) }
                }
            }

            separator()

            item(messages["bank.account.transactions.table.context.menu.details"]) {
                action {
                    selectedItems.forEach { presenter.showTransactionDetailsWindow(it) }
                }
            }
        }

        return contextMenu
    }

    fun addToExpendituresAndRevenues(transactions: List<BankAccountTransaction>) {
        overviewPresenter.addToExpendituresAndRevenues(transactions)
    }

    private fun adjustBeforeAddingToExpendituresAndRevenues(transactions: List<BankAccountTransaction>) {
        overviewPresenter.adjustBeforeAddingToExpendituresAndRevenues(transactions)
    }

    private fun getResourceKeyForDirectlyAddingToExpendituresOrRevenues(type: ExpenditureRevenueType): String {
        return when (type) {
            ExpenditureRevenueType.Expenditures -> "bank.account.transactions.table.context.menu.add.to.expenditures"
            ExpenditureRevenueType.Revenues -> "bank.account.transactions.table.context.menu.add.to.revenues"
            ExpenditureRevenueType.ExpendituresAndRevenues -> "bank.account.transactions.table.context.menu.add.to.expenditures.and.revenues"
        }
    }

    private fun getResourceKeyForAdjustBeforeAddingToExpendituresOrRevenues(type: ExpenditureRevenueType): String {
        return when (type) {
            ExpenditureRevenueType.Expenditures -> "bank.account.transactions.table.context.menu.adjust.before.adding.to.expenditures"
            ExpenditureRevenueType.Revenues -> "bank.account.transactions.table.context.menu.adjust.before.adding.to.revenues"
            ExpenditureRevenueType.ExpendituresAndRevenues -> "bank.account.transactions.table.context.menu.adjust.before.adding.to.expenditures.and.revenues"
        }
    }

    private fun getExpenditureRevenueType(selectedItems: List<BankAccountTransaction>): ExpenditureRevenueType {
        val countExpenditures = selectedItems.filter { it.isDebit }.count()

        return when (countExpenditures) {
            selectedItems.size -> ExpenditureRevenueType.Expenditures // only expenditures in selectedItems
            0 -> ExpenditureRevenueType.Revenues // only revenues in selectedItems
            else -> ExpenditureRevenueType.ExpendituresAndRevenues
        }
    }


    private fun createTooltipForCreatedDocument(document: Document): Tooltip {
        return Tooltip(
            String.format(
                messages["bank.account.transactions.table.created.document.from.transaction"],
                ValueDateFormat.format(document.paymentDate),
                overviewPresenter.getCurrencyString(document.totalAmount),
                document.description
            )
        )
    }

    private fun showCreatedDocuments(selectedItems: List<BankAccountTransaction>) {
        val createdDocuments = selectedItems.mapNotNull { it.createdDocument }

        createdDocuments.forEach { document ->
            overviewPresenter.showEditDocumentWindow(document)
        }
    }

}