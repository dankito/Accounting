package net.dankito.accounting.javafx.windows.banking.controls

import javafx.beans.binding.ObjectBinding
import javafx.collections.ObservableList
import javafx.geometry.Pos
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.util.Callback
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import tornadofx.*
import tornadofx.FX.Companion.messages
import java.text.DateFormat


class BankAccountTransactionsTable(private val overviewPresenter: OverviewPresenter,
                                   transactionsToDisplay: ObservableList<BankAccountTransaction>
) : TableView<BankAccountTransaction>(transactionsToDisplay) {


    companion object {

        private val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    }


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
    }

}