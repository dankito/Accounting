package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.collections.ObservableList
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.StyleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.javafx.ui.extensions.currencyColumn
import net.dankito.utils.javafx.ui.extensions.dateColumn
import net.dankito.utils.javafx.ui.extensions.initiallyUseRemainingSpace
import tornadofx.FX.Companion.messages
import tornadofx.column
import tornadofx.get
import tornadofx.onDoubleClick
import tornadofx.selectedItem


open class DocumentsTable(
    displayedDocuments: ObservableList<Document>,
    protected val styleDocumentsFromCurrentAndPreviousPeriod: Boolean,
    protected val presenter: OverviewPresenter,
    protected val styleService: StyleService,
    protected val threadPool: IThreadPool
) : TableView<Document>(displayedDocuments) {


    init {
        initUi()
    }


    private fun initUi() {
        column(messages["description"], Document::description) {
            this.initiallyUseRemainingSpace(this@DocumentsTable)
        }

        dateColumn(messages["main.window.documents.table.payment.date.column.header"], Document::paymentDate)

        currencyColumn(messages["net.amount"], Document::netAmount, OverviewPresenter.CurrencyFormat)

        currencyColumn(messages["value.added.tax"], Document::valueAddedTax, OverviewPresenter.CurrencyFormat)

        currencyColumn(messages["total.amount"], Document::totalAmount, OverviewPresenter.CurrencyFormat)


        selectionModel.selectionMode = SelectionMode.MULTIPLE


        onDoubleClick {
            selectedItem?.let { clickedItem -> presenter.showEditDocumentWindow(clickedItem) }
        }

        setOnKeyReleased { event -> keyPressed(event, selectionModel.selectedItems) }

        setRowFactory { object : TableRow<Document>() {

            override fun updateItem(item: Document?, empty: Boolean) {
                super.updateItem(item, empty)

                style = getRowStyle(item)
            }
        } }
    }


    protected open fun getRowStyle(item: Document?): String {
        if (styleDocumentsFromCurrentAndPreviousPeriod) {
            item?.let { document ->
                if (presenter.isInCurrentAccountingPeriod(document)) {
                    return styleService.currentAccountingPeriodStyle
                }
                else if (presenter.isInPreviousAccountingPeriod(document)) {
                    return styleService.previousAccountingPeriodStyle
                }
            }
        }

        return styleService.defaultStyle
    }


    private fun keyPressed(event: KeyEvent, selectedItems: List<Document>) {
        if (event.code == KeyCode.DELETE) {
            deleteDocumentsAsync(selectedItems)
        }
    }

    private fun deleteDocumentsAsync(documentsToDelete: List<Document>) {
        // TODO: ask user first
        threadPool.runAsync {
            deleteDocuments(documentsToDelete)
        }
    }

    private fun deleteDocuments(documentsToDelete: List<Document>) {
        presenter.delete(documentsToDelete)
    }

}