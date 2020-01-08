package net.dankito.accounting.javafx.windows.document

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import javafx.util.converter.NumberStringConverter
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.data.model.invoice.InvoiceData
import net.dankito.accounting.javafx.controls.VatRateComboBox
import net.dankito.accounting.javafx.controls.vatRateComboBox
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.document.model.DocumentItemViewItem
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.javafx.ui.controls.DoubleTextField
import net.dankito.utils.javafx.ui.controls.addButton
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.currencyColumn
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*
import java.time.LocalDate


class EditDocumentWindow(private val document: Document, private val presenter: OverviewPresenter, private val extractedData: InvoiceData? = null) : Window() {

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

        private const val EditDocumentItemPrefWidth = 300.0

        private const val ButtonsHeight = 38.0
        private const val ButtonsWidth = 120.0
        private const val EditItemsButtonsWidth = 140.0
        private const val ButtonsHorizontalSpace = 12.0

    }


    private val documentDescription = SimpleStringProperty(document.description)

    private val paymentDate = SimpleObjectProperty<LocalDate>(document.paymentDate.asLocalDate() ?: LocalDate.now())

    private val hasMultipleDocumentItems = SimpleBooleanProperty(document.items.size > 1)

    private val editDocumentItems = SimpleBooleanProperty()

    private val vatRate = SimpleFloatProperty(
        extractedData?.data?.potentialValueAddedTaxRate?.amount?.toFloat()
        ?: document.valueAddedTaxRates.firstOrNull()
        ?: presenter.getDefaultVatRateForUser()
    )

    private val documentItems = FXCollections.observableArrayList<DocumentItemViewItem>(document.items.map { mapToViewItem(it) })

    private lateinit var tableViewDocumentItems: TableView<DocumentItemViewItem>

    private val totalAmount = SimpleDoubleProperty(extractedData?.data?.potentialTotalAmount?.amount ?: document.totalAmount)

    private val hasTotalAmountSuggestions = extractedData?.data?.allAmounts?.isNotEmpty() ?: false


    private var documentViewAndEditDocumentItemSplitPane: SplitPane by singleAssign()

    private var editDocumentItemPane: Node by singleAssign()

    private val showEditDocumentItemPane = SimpleBooleanProperty()

    private var editedDocumentItem: DocumentItemViewItem? = null

    private val documentItemDescriptionTextField = TextField()

    private val documentItemVatRateComboBox = VatRateComboBox(SimpleFloatProperty(presenter.getDefaultVatRateForUser()), presenter.getVatRatesForUser())

    private val documentItemGrossAmount = SimpleDoubleProperty()

    private val documentItemGrossAmountTextField: DoubleTextField = doubleTextfield(documentItemGrossAmount, false, 2)


    init {
        showEditDocumentItemPane.addListener { _, _, newValue -> updateShowEditDocumentItemPane(newValue) }
    }


    override val root = vbox {

        documentViewAndEditDocumentItemSplitPane = splitpane {

            vbox {
                prefWidth = 475.0

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

                    val vatRates = HashSet(presenter.getVatRatesForUser())
                    extractedData?.data?.percentages?.map { it.amount.toFloat() }?.let { vatRates.addAll(it) }
                    vatRateComboBox(vatRate, vatRates.toList()) {
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
                            action { addNewDocumentItem() }

                            anchorpaneConstraints {
                                topAnchor = 2.0
                                rightAnchor = 0.0
                                bottomAnchor = 2.0
                            }
                        }
                    }

                    tableViewDocumentItems = tableview<DocumentItemViewItem>(documentItems) {
                        column<DocumentItemViewItem, String>(messages["edit.document.window.document.items.table.description.column.header"], DocumentItemViewItem::description)

                        currencyColumn(messages["net.amount"], DocumentItemViewItem::netAmount, OverviewPresenter.CurrencyFormat)

                        currencyColumn(messages["value.added.tax"], DocumentItemViewItem::vat, OverviewPresenter.CurrencyFormat)

                        currencyColumn(messages["edit.document.window.document.items.table.total.amount.column.header"], DocumentItemViewItem::grossAmount, OverviewPresenter.CurrencyFormat)


                        prefHeight = TableDocumentItemsInitialHeight

                        selectionModel.selectionMode = SelectionMode.SINGLE

                        selectionModel.selectedItemProperty().addListener { _, _, newValue -> selectedDocumentItemChanged(newValue) }

                        setOnKeyReleased { event -> documentItemsTableKeyPressed(event, selectionModel.selectedItem) }
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

                        isVisible = !!! hasTotalAmountSuggestions
                        ensureOnlyUsesSpaceIfVisible()

                        editableWhen(editDocumentItems.not())

                        anchorpaneConstraints {
                            topAnchor = 0.0
                            rightAnchor = CurrencyLabelWidth + CurrencyLabelLeftMargin
                            bottomAnchor = 0.0
                        }
                    }

                    combobox(SimpleDoubleProperty(), extractedData?.data?.allAmounts?.map { it.amount }?.toSet()?.toList()) {
                        minWidth = AmountTextFieldsWidth
                        maxWidth = AmountTextFieldsWidth

                        converter = NumberStringConverter()

                        isVisible = hasTotalAmountSuggestions
                        ensureOnlyUsesSpaceIfVisible()

                        isEditable = true // isEditable sets bound value to 0.0 !
                        disableWhen(editDocumentItems)

                        valueProperty().bindBidirectional(totalAmount) // so bind to totalAmount after isEditable is set

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
            }

            editDocumentItemPane = vbox {
                prefWidth = 350.0

                visibleWhen(showEditDocumentItemPane)
                ensureOnlyUsesSpaceIfVisible()

                SplitPane.setResizableWithParent(this, false)

                form {
                    fieldset(messages["edit.document.window.edit.document.item.label"]) {
                        field(messages["description"]) {
                            add(documentItemDescriptionTextField)
                        }

                        field(messages["value.added.tax.rate"]) {
                            add(documentItemVatRateComboBox.apply {
                                // don't know why but selected vat rate doesn't get applied immediately to editedDocumentItem.vatRate so i have to use runLater { }
                                valueProperty().addListener { _, _, _ -> runLater { updateVatAndNetAmount() } }
                            })
                        }

                        field(messages["gross.amount"]) {
                            add(documentItemGrossAmountTextField.apply {
                                textProperty().addListener { _, _, _ -> updateVatAndNetAmount() }
                            })
                        }
                    }
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
                disableWhen(hasMultipleDocumentItems)

                selectedProperty().addListener { _, _, newValue -> toggleShowEditDocumentItemsControls(newValue) }

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

        documentViewAndEditDocumentItemSplitPane.items.remove(editDocumentItemPane)

        runLater {
            if (hasMultipleDocumentItems.value) {
                editDocumentItems.value = true
                showEditDocumentItemPane.value = true
                tableViewDocumentItems.selectionModel.select(documentItems.first())
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


    private fun VBox.toggleShowEditDocumentItemsControls(showEditDocumentItemsControls: Boolean) {
        editDocumentItems.set(showEditDocumentItemsControls)

        if (showEditDocumentItemsControls) {
            if (documentItems.isEmpty()) {
                addNewDocumentItem()
            }
            else if (editedDocumentItem != null) {
                showEditDocumentItemPane.value = true
            }
        }
        else {
            showEditDocumentItemPane.value = false
        }

        val newPrefHeight = if (showEditDocumentItemsControls) WindowHeightWhenShowingDocumentItems else NormalWindowHeight
        this.prefHeight = newPrefHeight

        updateWindowSize()
    }

    private fun updateShowEditDocumentItemPane(showEditDocumentItemPane: Boolean) {
        if (showEditDocumentItemPane) {
            documentViewAndEditDocumentItemSplitPane.items.add(editDocumentItemPane)
            documentViewAndEditDocumentItemSplitPane.prefWidth = documentViewAndEditDocumentItemSplitPane.width + EditDocumentItemPrefWidth
        } else {
            documentViewAndEditDocumentItemSplitPane.items.remove(editDocumentItemPane)
            documentViewAndEditDocumentItemSplitPane.prefWidth = documentViewAndEditDocumentItemSplitPane.width - EditDocumentItemPrefWidth
        }

        updateWindowSize()

        if (showEditDocumentItemPane) {
            runLater {
                documentViewAndEditDocumentItemSplitPane.setDividerPosition(0, 1.0 - (EditDocumentItemPrefWidth / documentViewAndEditDocumentItemSplitPane.prefWidth))
            }
        }
    }

    private fun updateWindowSize() {
        currentStage?.sizeToScene()
    }


    private fun mapToViewItem(item: DocumentItem): DocumentItemViewItem {
        return DocumentItemViewItem(item)
    }

    private fun selectedDocumentItemChanged(selectedItem: DocumentItemViewItem?) {
        editedDocumentItem?.let { item ->
            documentItemDescriptionTextField.textProperty().unbindBidirectional(item.description)
            documentItemVatRateComboBox.valueProperty().unbindBidirectional(item.vatRate)
            documentItemGrossAmount.unbindBidirectional(item.grossAmount)
        }

        showEditDocumentItemPane.value = selectedItem != null

        editedDocumentItem = selectedItem

        editedDocumentItem?.let { item ->
            documentItemDescriptionTextField.textProperty().bindBidirectional(item.description)
            documentItemVatRateComboBox.valueProperty().bindBidirectional(item.vatRate)
            documentItemGrossAmount.bindBidirectional(item.grossAmount)
        }
    }

    private fun documentItemsTableKeyPressed(event: KeyEvent, selectedItem: DocumentItemViewItem?) {
        selectedItem?.let {
            if (event.code == KeyCode.DELETE) {
                removeDocumentItem(selectedItem)
            }
        }
    }


    private fun addNewDocumentItem() {
        val newItem = mapToViewItem(DocumentItem(0.0, presenter.getDefaultVatRateForUser(), 0.0, 0.0))

        documentItems.add(newItem)

        if (editDocumentItems.value) {
            tableViewDocumentItems.selectionModel.select(newItem)
        }

        updateHasMultipleDocumentItems()
    }

    private fun removeDocumentItem(selectedItem: DocumentItemViewItem) {
        if (documentItems.size > 1) { // don't remove last DocumentItem
            documentItems.remove(selectedItem)

            updateHasMultipleDocumentItems()
        }
    }

    private fun updateHasMultipleDocumentItems() {
        hasMultipleDocumentItems.value = documentItems.size > 1
    }


    private fun updateVatAndNetAmount() {
        editedDocumentItem?.let { itemViewItem ->
            val tempItem = DocumentItem(itemViewItem.vatRate.value, itemViewItem.grossAmount.value)

            presenter.updateVat(tempItem)

            itemViewItem.netAmount.value = tempItem.netAmount
            itemViewItem.vat.value = tempItem.valueAddedTax

            updateTotalAmount()
        }
    }

    private fun updateTotalAmount() {
        totalAmount.value = documentItems.sumByDouble { it.grossAmount.value }
    }


    private fun saveAndClose() {
        document.description = documentDescription.value
        document.paymentDate = paymentDate.value.asUtilDate()

        if (editDocumentItems.get()) {
            document.items = documentItems.map {
                it.commit()
                it.item
            }
        }
        else if (document.items.isNotEmpty()) {
            val item = document.items.first()
            item.grossAmount = totalAmount.value
            item.valueAddedTaxRate = vatRate.value
        }
        else {
            document.addItem(DocumentItem(vatRate.value, totalAmount.value))
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