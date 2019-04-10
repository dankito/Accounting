package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.collections.FXCollections
import net.dankito.accounting.data.model.Document
import net.dankito.utils.javafx.ui.controls.AddButton
import net.dankito.utils.javafx.ui.extensions.concurrencyColumn
import net.dankito.utils.javafx.ui.extensions.dateColumn
import net.dankito.utils.javafx.ui.extensions.initiallyUseRemainingSpace
import tornadofx.*


open class DocumentsOverview(titleResourceKey: String) : View() {

    companion object {
        protected const val ControlBarHeight = 35.0

        protected const val TitleLabelLeftMargin = 2.0
        protected const val TitleLabelTopBottomMargin = 5.0

        protected const val AddDocumentButtonTopBottomMargin = 1.0
    }


    protected val documents = FXCollections.observableArrayList<Document>()


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

                anchorpaneConstraints {
                    topAnchor = AddDocumentButtonTopBottomMargin
                    rightAnchor = 0.0
                    bottomAnchor = AddDocumentButtonTopBottomMargin
                }
            })
        }


        tableview<Document>(documents) {
            column(messages["main.window.documents.table.description.column.header"], Document::documentDescription) {
                this.initiallyUseRemainingSpace(this@tableview)
            }

            dateColumn(messages["main.window.documents.table.payment.date.column.header"], Document::paymentDate)

            concurrencyColumn(messages["main.window.documents.table.net.amount.column.header"], Document::netAmount)

            concurrencyColumn(messages["main.window.documents.table.value.added.tax.column.header"], Document::valueAddedTax)

            concurrencyColumn(messages["main.window.documents.table.total.amount.column.header"], Document::totalAmount)

        }

    }

}