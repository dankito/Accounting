package net.dankito.accounting.javafx.windows.document

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.javafx.controls.vatRateComboBox
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.currencyColumn
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.extensions.initiallyUseRemainingSpace
import tornadofx.*
import java.time.LocalDate


class EditDocumentWindow(private val document: Document, private val presenter: OverviewPresenter) : Window() {

    companion object {

        private const val NormalWindowHeight = 174.0
        private const val WindowHeightWhenShowingDocumentItems = 288.0

        private const val FieldHeight = 30.0
        private const val FieldVerticalSpace = 4.0

        private const val LabelsWidth = 135.0

        private const val AmountTextFieldsWidth = 140.0

        private const val CurrencyLabelWidth = 10.0
        private const val CurrencyLabelLeftMargin = 4.0

        private const val DatePickerWidth = AmountTextFieldsWidth + CurrencyLabelWidth + CurrencyLabelLeftMargin

        private const val TableDocumentItemsInitialHeight = 114.0

        private const val ButtonsHeight = 38.0
        private const val ButtonsWidth = 120.0
        private const val EditItemsButtonsWidth = 140.0
        private const val ButtonsHorizontalSpace = 12.0

    }


    private val documentDescription = SimpleStringProperty(document.description)

    private val paymentDate = SimpleObjectProperty<LocalDate>(document.paymentDate.asLocalDate() ?: LocalDate.now())

    private val editDocumentItems = SimpleBooleanProperty(false)

    private val vatRate = SimpleFloatProperty(if (document.isValueAddedTaxRateSet) document.valueAddedTaxRate
                                                else presenter.getDefaultVatRateForUser())

    private val documentItems = FXCollections.observableArrayList<DocumentItem>()

    private lateinit var tableViewDocumentItems: TableView<DocumentItem>

    private val totalAmount = SimpleDoubleProperty(if (document.isTotalAmountSet) document.totalAmount else 0.0)


    override val root = vbox {

        prefWidth = 450.0

        hbox {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.document.description.label")

            textfield(documentDescription) {
                minHeight = FieldHeight
                maxHeight = FieldHeight

                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
        }

        anchorpane {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.payment.date.label")

            datepicker(paymentDate) {
                minWidth = DatePickerWidth
                maxWidth = DatePickerWidth

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

        anchorpane {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            hiddenWhen(editDocumentItems)
            ensureOnlyUsesSpaceIfVisible()

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("value.added.tax.rate")

            vatRateComboBox(vatRate, presenter.getVatRatesForUser()) {
                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

        vbox {
            useMaxWidth = true
            useMaxHeight = true
            prefHeight = FieldHeight + TableDocumentItemsInitialHeight - 5.0

            visibleWhen(editDocumentItems)
            ensureOnlyUsesSpaceIfVisible()

            vboxConstraints { marginBottom = FieldVerticalSpace }

            anchorpane {
                label(messages["edit.document.window.document.items.label"]) {
                    anchorpaneConstraints {
                        topAnchor = 0.0
                        leftAnchor = 0.0
                        bottomAnchor = 0.0
                    }
                }

                addButton {
                    action { documentItems.add(DocumentItem()) }

                    anchorpaneConstraints {
                        topAnchor = 2.0
                        rightAnchor = 0.0
                        bottomAnchor = 2.0
                    }
                }
            }

            tableViewDocumentItems = tableview<DocumentItem>(documentItems) {
                column(messages["edit.document.window.document.items.table.description.column.header"], DocumentItem::description) {
                    this.initiallyUseRemainingSpace(this@tableview)

                    cellFormat {
                        graphic = textfield(rowItem.description) {
                            minWidth = 150.0
                            prefHeight = 30.0
                        }
                    }
                }

                currencyColumn(messages["value.added.tax"], DocumentItem::valueAddedTax, OverviewPresenter.CurrencyFormat) {
                    isEditable = true
                }

                currencyColumn(messages["edit.document.window.document.items.table.total.amount.column.header"], DocumentItem::totalAmount, OverviewPresenter.CurrencyFormat) {
                    isEditable = true
                }


                prefHeight = TableDocumentItemsInitialHeight

                selectionModel.selectionMode = SelectionMode.MULTIPLE

                setOnKeyReleased { event -> documentItemsTableKeyPressed(event, selectionModel.selectedItems) }
            }
        }

        anchorpane {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.total.amount.label")

            doubleTextfield(totalAmount, false, 2) {
                minWidth = AmountTextFieldsWidth
                maxWidth = AmountTextFieldsWidth

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = CurrencyLabelWidth + CurrencyLabelLeftMargin
                    bottomAnchor = 0.0
                }
            }

            label(presenter.getUserCurrencySymbol()) {
                prefWidth = CurrencyLabelWidth

                textAlignment = TextAlignment.RIGHT

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

        anchorpane {
            minHeight = ButtonsHeight
            maxHeight = ButtonsHeight
            useMaxWidth = true

            togglebutton(messages["edit.document.window.edit.document.items.label"]) {
                minWidth = EditItemsButtonsWidth
                maxWidth = EditItemsButtonsWidth

                isSelected = editDocumentItems.get()

                selectedProperty().addListener { _, _, newValue ->
                    editDocumentItems.set(newValue)

                    if (newValue && documentItems.isEmpty()) {
                        documentItems.add(DocumentItem())
                    }

//                    val newPrefHeight = if (newValue) this@vbox.height + tableViewDocumentItems.height else this@vbox.height - tableViewDocumentItems.height
                    val newPrefHeight = if (newValue) WindowHeightWhenShowingDocumentItems else NormalWindowHeight
                    this@vbox.prefHeight = newPrefHeight
                    currentStage?.sizeToScene()
                }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    leftAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["ok"]) {
                minWidth = ButtonsWidth
                maxWidth = ButtonsWidth

                action { saveAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["cancel"]) {
                minWidth = ButtonsWidth
                maxWidth = ButtonsWidth

                action { askIfChangesShouldBeSavedAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = ButtonsWidth + ButtonsHorizontalSpace
                    bottomAnchor = 0.0
                }
            }
        }
    }

    fun EventTarget.label(textResourceKey: String = ""): Label {
        return label(messages[textResourceKey], null).apply {
            useMaxHeight = true
            minWidth = LabelsWidth
            maxHeight = LabelsWidth

            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
            }
        }
    }


    private fun documentItemsTableKeyPressed(event: KeyEvent, selectedItems: List<DocumentItem>) {
        if (event.code == KeyCode.DELETE) {
            documentItems.removeAll(selectedItems)
        }
    }


    private fun saveAndClose() {
        document.description = documentDescription.value
        document.paymentDate = paymentDate.value.asUtilDate()
        document.totalAmount = totalAmount.value

        if (editDocumentItems.get()) {
            document.items = documentItems.toList()
        }
        else {
            document.valueAddedTaxRate = vatRate.value
        }

        presenter.saveOrUpdate(document)

        close()
    }

    private fun askIfChangesShouldBeSavedAndClose() {
        // TODO: check if changes are made and if so ask user if he/she likes to save them

        if (document.isPersisted() == false) {
            // we were trying to create a document from an bank account transaction.
            // but as user doesn't want to save it now, remove the document reference from transaction
            document.createdFromAccountTransaction?.let { transaction ->
                transaction.createdDocument = null

                document.createdFromAccountTransaction = null
            }
        }

        close()
    }

}