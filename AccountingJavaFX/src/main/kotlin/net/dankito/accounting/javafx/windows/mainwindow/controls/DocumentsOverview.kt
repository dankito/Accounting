package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.collections.FXCollections
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableRow
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.event.AccountingPeriodChangedEvent
import net.dankito.accounting.data.model.event.DocumentsUpdatedEvent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.StyleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.events.IEventBus
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.searchtextfield
import net.dankito.utils.javafx.ui.extensions.currencyColumn
import net.dankito.utils.javafx.ui.extensions.dateColumn
import net.dankito.utils.javafx.ui.extensions.initiallyUseRemainingSpace
import tornadofx.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


abstract class DocumentsOverview(titleResourceKey: String, protected val presenter: OverviewPresenter) : View() {

    companion object {
        protected const val ControlBarHeight = 35.0

        protected const val TitleLabelLeftMargin = 2.0
        protected const val TitleLabelTopBottomMargin = 5.0

        protected const val SearchFieldWidth = 200.0
        protected const val SearchFieldTopBottomMargin = 3.0

        protected const val AddDocumentButtonWidth = ControlBarHeight
        protected const val AddDocumentButtonTopBottomMargin = 1.0
    }


    protected abstract fun retrieveDocuments(): List<Document>

    protected abstract fun showCreateNewDocumentWindow()


    @Inject
    protected lateinit var styleService: StyleService

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var threadPool: IThreadPool


    protected var tableView: TableView<Document> by singleAssign()

    protected var allDocumentsOfType = listOf<Document>()

    protected val displayedDocuments = FXCollections.observableArrayList<Document>()

    protected var currentDocumentsFilterTerm = ""


    init {
        AppComponent.component.inject(this)

        eventBus.subscribe(DocumentsUpdatedEvent::class.java) {
            runLater { loadDocumentsInBackgroundAndUpdateOnUiThread() }
        }

        eventBus.subscribe(AccountingPeriodChangedEvent::class.java) {
            runLater { updateTableView() }
        }

        loadDocumentsInBackgroundAndUpdateOnUiThreadDelayed() // so that subclasses have time to initialize
    }


    override val root = vbox {
        val title = messages[titleResourceKey]

        anchorpane {
            minHeight = ControlBarHeight
            maxHeight = ControlBarHeight

            label(title) {
                font = Font.font(font.family, FontWeight.BLACK, font.size + 1)

                anchorpaneConstraints {
                    leftAnchor = TitleLabelLeftMargin
                    topAnchor = TitleLabelTopBottomMargin
                    bottomAnchor = TitleLabelTopBottomMargin
                }
            }

            // TODO: may add icon (Lupe) before search field
            searchtextfield {
                minWidth = SearchFieldWidth
                maxWidth = SearchFieldWidth

                promptText = String.format(messages["main.window.documents.overview.search.documents.prompt"], title)

                textProperty().addListener { _, _, newValue -> filterDocuments(newValue) }

                anchorpaneConstraints {
                    topAnchor = SearchFieldTopBottomMargin
                    rightAnchor = AddDocumentButtonWidth + 18.0
                    bottomAnchor = SearchFieldTopBottomMargin
                }
            }

            addButton {
                minWidth = AddDocumentButtonWidth

                action { showCreateNewDocumentWindow() }

                anchorpaneConstraints {
                    topAnchor = AddDocumentButtonTopBottomMargin
                    rightAnchor = 0.0
                    bottomAnchor = AddDocumentButtonTopBottomMargin
                }
            }
        }


        tableView = tableview<Document>(displayedDocuments) {
            column(messages["main.window.documents.table.description.column.header"], Document::description) {
                this.initiallyUseRemainingSpace(this@tableview)
            }

            dateColumn(messages["main.window.documents.table.payment.date.column.header"], Document::paymentDate)

            currencyColumn(messages["main.window.documents.table.net.amount.column.header"], Document::netAmount, OverviewPresenter.CurrencyFormat)

            currencyColumn(messages["value.added.tax"], Document::valueAddedTax, OverviewPresenter.CurrencyFormat)

            currencyColumn(messages["main.window.documents.table.total.amount.column.header"], Document::totalAmount, OverviewPresenter.CurrencyFormat)


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

    }


    private fun loadDocumentsInBackgroundAndUpdateOnUiThreadDelayed() {
        Timer().schedule(1000) {
            loadDocumentsInBackgroundAndUpdateOnUiThread()
        }
    }

    protected open fun loadDocumentsInBackgroundAndUpdateOnUiThread() {
        val retrievedDocuments = retrieveDocuments()

        retrievedDocumentsOffUiThread(retrievedDocuments)
    }

    protected open fun retrievedDocumentsOffUiThread(retrievedDocuments: List<Document>) {
        allDocumentsOfType = sortDocuments(retrievedDocuments)

        runLater {
            reapplyLastSearch()
        }
    }

    protected open fun sortDocuments(retrievedDocuments: List<Document>): List<Document> {
        return retrievedDocuments.sortedByDescending { it.paymentDate }
    }


    protected open fun reapplyLastSearch() {
        filterDocuments(currentDocumentsFilterTerm)
    }

    protected open fun filterDocuments(filterTerm: String) {
        currentDocumentsFilterTerm = filterTerm

        displayedDocuments.setAll(presenter.filterDocuments(allDocumentsOfType, filterTerm))
    }


    protected open fun updateTableView() {
        tableView.refresh()
    }

    protected open fun getRowStyle(item: Document?): String {
        item?.let { document ->
            if (presenter.isInCurrentAccountingPeriod(document)) {
                return styleService.currentAccountingPeriodStyle
            }
            else if (presenter.isInPreviousAccountingPeriod(document)) {
                return styleService.previousAccountingPeriodStyle
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