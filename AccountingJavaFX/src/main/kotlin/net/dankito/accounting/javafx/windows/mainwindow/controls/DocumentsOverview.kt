package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.collections.FXCollections
import javafx.scene.control.SelectionMode
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.event.DocumentsUpdatedEvent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.utils.IThreadPool
import net.dankito.utils.events.IEventBus
import net.dankito.utils.javafx.ui.controls.AddButton
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

        protected const val AddDocumentButtonTopBottomMargin = 1.0
    }


    protected abstract fun retrieveDocuments(): List<Document>

    protected abstract fun showCreateNewDocumentWindow()


    @Inject
    lateinit var eventBus: IEventBus

    @Inject
    lateinit var threadPool: IThreadPool


    protected val documents = FXCollections.observableArrayList<Document>()


    init {
        AppComponent.component.inject(this)

        eventBus.subscribe(DocumentsUpdatedEvent::class.java) {
            runLater { loadDocumentsInBackgroundAndUpdateOnUiThread() }
        }

        loadDocumentsInBackgroundAndUpdateOnUiThreadDelayed() // so that subclasses have time to initialize
    }


    override val root = vbox {
        anchorpane {
            minHeight = ControlBarHeight
            maxHeight = ControlBarHeight

            label(FX.messages[titleResourceKey]) {
                anchorpaneConstraints {
                    leftAnchor = TitleLabelLeftMargin
                    topAnchor = TitleLabelTopBottomMargin
                    bottomAnchor = TitleLabelTopBottomMargin
                }
            }

            add(AddButton().apply {
                minWidth = ControlBarHeight

                action { showCreateNewDocumentWindow() }

                anchorpaneConstraints {
                    topAnchor = AddDocumentButtonTopBottomMargin
                    rightAnchor = 0.0
                    bottomAnchor = AddDocumentButtonTopBottomMargin
                }
            })
        }


        tableview<Document>(documents) {
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
        }

    }


    private fun loadDocumentsInBackgroundAndUpdateOnUiThreadDelayed() {
        Timer().schedule(1000) {
            loadDocumentsInBackgroundAndUpdateOnUiThread()
        }
    }

    protected open fun loadDocumentsInBackgroundAndUpdateOnUiThread() {
        val retrievedDocuments = retrieveDocuments().sortedBy { it.paymentDate }

        val documentsInCurrentAccountingPeriod = getDocumentsInCurrentAndPreviousAccountingPeriod(retrievedDocuments)

        runLater { documents.setAll(documentsInCurrentAccountingPeriod) }
    }

    protected open fun getDocumentsInCurrentAndPreviousAccountingPeriod(retrievedDocuments: List<Document>) =
        presenter.getDocumentsInCurrentAndPreviousAccountingPeriod(retrievedDocuments)


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