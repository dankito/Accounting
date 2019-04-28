package net.dankito.accounting.javafx.windows.invoice

import javafx.beans.property.SimpleDoubleProperty
import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.stage.FileChooser
import javafx.util.converter.PercentageStringConverter
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TrackedMonth
import net.dankito.accounting.data.model.timetracker.TrackedTimes
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.extensions.asLocalDate
import net.dankito.accounting.javafx.presenter.CreateInvoicePresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.presenter.TimeTrackerAccountPresenter
import net.dankito.accounting.javafx.windows.invoice.controls.SelectFileView
import net.dankito.accounting.javafx.windows.invoice.controls.TrackedMonthListCellFragment
import net.dankito.accounting.javafx.windows.invoice.model.CreateInvoiceSettingsViewModel
import net.dankito.accounting.javafx.windows.invoice.model.InvoiceViewModel
import net.dankito.accounting.javafx.windows.invoice.model.SelectFileType
import net.dankito.accounting.javafx.windows.invoice.model.TrackedMonthItemViewModel
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import org.slf4j.LoggerFactory
import tornadofx.*
import java.time.format.DateTimeFormatter
import javax.inject.Inject


class CreateInvoiceWindow : Window() {

    companion object {

        private const val TimeTrackerAccountButtonsWidth = 110.0

        private const val ButtonWidth = 150.0

        private const val ButtonHeight = 40.0

        private const val ButtonBottomAnchor = 24.0


        private val MonthDateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

        private val InvoiceDateFormatter = DateTimeFormatter.ofPattern("yyMMdd'1'")

        private val logger = LoggerFactory.getLogger(CreateInvoiceWindow::class.java)

    }


    @Inject
    lateinit var presenter: CreateInvoicePresenter

    @Inject
    lateinit var timeTrackerAccountPresenter: TimeTrackerAccountPresenter

    @Inject
    lateinit var overviewPresenter: OverviewPresenter


    private val settings: CreateInvoiceSettings

    private val settingsViewModel: CreateInvoiceSettingsViewModel

    private val invoiceViewModel = InvoiceViewModel()


    private val timeTrackerAccounts = FXCollections.observableArrayList<TimeTrackerAccount>()

    private val trackedMonths = FXCollections.observableArrayList<TrackedMonth>()

    private val selectedTrackedMonth = TrackedMonthItemViewModel()

    private val invoiceItemQuantity = SimpleDoubleProperty()


    private var selectInvoiceTemplateFileView: SelectFileView by singleAssign()

    private var selectInvoiceOutputFileView: SelectFileView by singleAssign()


    init {
        AppComponent.component.inject(this)

        settings = presenter.settings

        settingsViewModel = CreateInvoiceSettingsViewModel(settings)

        showAvailableTimeTrackerAccounts()

        settings.timeTrackerAccount?.let { account ->
            account.trackedTimes?.let { showTrackedMonths(it) }

            importTimeTrackerData()
        }

        updateInvoiceDescription()

        settingsViewModel.clientName.addListener { _, _, _ -> updateInvoiceDescription() }

        updateInvoiceNumber()

        invoiceViewModel.invoicingDate.addListener { _, _, _ -> updateInvoiceNumber() }

        setupSelectFileViews()
    }

    override val root = form {
        fieldset(messages["create.invoice.window.import.data.section.title"]) {
            field(messages["create.invoice.window.time.tracker.account"]) {
                combobox(settingsViewModel.timeTrackerAccount, timeTrackerAccounts) {
                    prefWidth = 200.0

                    cellFormat { text = it.accountName }

                    valueProperty().addListener { _, _, newValue ->
                        settingsViewModel.isATimeTrackerAccountSelected.value = newValue != null
                    }
                }

                button(messages["edit..."]) {
                    prefWidth = TimeTrackerAccountButtonsWidth

                    enableWhen(settingsViewModel.isATimeTrackerAccountSelected)

                    action { editSelectedTimeTrackerAccount() }
                }

                button(messages["new..."]) {
                    prefWidth = TimeTrackerAccountButtonsWidth

                    action { createNewTimeTrackerAccount() }
                }
            }

            vbox {
                visibleWhen(settingsViewModel.isATimeTrackerAccountSelected)

                ensureOnlyUsesSpaceIfVisible()

                borderpane {
                    minHeight = 32.0

                    vboxConstraints {
                        marginBottom = 6.0
                    }

                    left {
                        hbox {
                            useMaxHeight = true
                            alignment = Pos.CENTER_LEFT

                            label(messages["create.invoice.window.time.tracker.last.updated"])

                            label() // TODO: show last updated date
                        }
                    }
                    right {
                        button(messages["update..."]) {
                            useMaxHeight = true
                            prefWidth = 120.0

                            action { importTimeTrackerData() }
                        }
                    }
                }

                listview(trackedMonths) {
                    minHeight = 110.0
                    maxHeight = minHeight
                    useMaxWidth = true

                    bindSelected(selectedTrackedMonth)

                    cellFragment(TrackedMonthListCellFragment::class)

                    onDoubleClick { selectedItem?.let { selectedTrackedMonthChanged(it) } }

                }
            }
        }

        fieldset(messages["create.invoice.window.recipient.section.title"]) {
            field(messages["create.invoice.window.recipient.name"]) {
                textfield(settingsViewModel.clientName)
            }

            field(messages["create.invoice.window.recipient.address.street.name.and.number"]) {
                textfield(settingsViewModel.clientAddressStreetName)

                textfield(settingsViewModel.clientAddressStreetNumber) {
                    minWidth = 60.0
                    maxWidth = minWidth
                }
            }

            field(messages["create.invoice.window.recipient.address.postal.code.and.city"]) {
                textfield(settingsViewModel.clientAddressPostalCode) {
                    minWidth = 60.0
                    maxWidth = minWidth
                }

                textfield(settingsViewModel.clientAddressCity)
            }
        }

        fieldset(messages["create.invoice.window.invoice.settings"]) {
            field(messages["create.invoice.window.invoice.description"]) {
                textfield(invoiceViewModel.invoiceDescription) {
                    promptText = messages["create.invoice.window.invoice.description.hint"]
                }
            }

            field(messages["create.invoice.window.invoicing.date"]) {
                datepicker(invoiceViewModel.invoicingDate)
            }

            field(messages["create.invoice.window.invoice.number"]) {
                textfield(invoiceViewModel.invoiceNumber)
            }

            field(messages["create.invoice.window.invoice.start.date"]) {
                datepicker(invoiceViewModel.invoiceStartDate)
            }

            field(messages["create.invoice.window.invoice.end.date"]) {
                datepicker(invoiceViewModel.invoiceEndDate)
            }
        }

        fieldset(messages["create.invoice.window.invoice.items.settings"]) {
            field(messages["create.invoice.window.invoice.item.description"]) {
                textfield(settingsViewModel.invoiceItemDescription) {
                    this.promptText = messages["create.invoice.window.invoice.item.description.prompt.text"]
                }
            }

            field(messages["create.invoice.window.invoice.item.unit.price"]) {
                doubleTextfield(settingsViewModel.invoiceItemUnitPrice)
            }

            field(messages["create.invoice.window.invoice.item.quantity"]) {
                doubleTextfield(invoiceItemQuantity)
            }

            field(messages["create.invoice.window.invoice.value.added.tax"]) {
                textfield().bind(settingsViewModel.valueAddedTax, false, PercentageStringConverter())
            }
        }

        add(selectInvoiceTemplateFileView.apply {
            root.vboxConstraints {
                useMaxWidth = true
                margin = Insets(6.0, 4.0, 0.0, 0.0)
            }
        })

        add(selectInvoiceOutputFileView.apply {
            root.vboxConstraints {
                useMaxWidth = true
                margin = Insets(6.0, 4.0, 6.0, 0.0)
            }
        })

        anchorpane {
            useMaxHeight = true

            button(messages["create.invoice.window.create.invoice"]) {
                minHeight = ButtonHeight
                maxHeight = minHeight
                minWidth = ButtonWidth
                maxWidth = minWidth

                action {
                    createInvoice()
                }

                anchorpaneConstraints {
                    rightAnchor = 0.0
                    bottomAnchor = ButtonBottomAnchor
                }
            }

            button(messages["cancel"]) {
                minHeight = ButtonHeight
                maxHeight = minHeight
                minWidth = ButtonWidth
                maxWidth = minWidth

                action {
                    closeWindow()
                }

                anchorpaneConstraints {
                    rightAnchor = ButtonWidth + 12.0
                    bottomAnchor = ButtonBottomAnchor
                }
            }
        }
    }


    private fun setupSelectFileViews() {
        selectInvoiceTemplateFileView = SelectFileView(messages["create.invoice.window.invoice.template.file"],
            SelectFileType.SelectFile, settings.invoiceTemplateFilePath,
            listOf(FileChooser.ExtensionFilter(messages["create.invoice.window.odt.extension.filter.description"], "*.odt"))) { path ->
            settings.invoiceTemplateFilePath = path
            presenter.saveSettings()
        }

        selectInvoiceOutputFileView = SelectFileView(messages["create.invoice.window.invoice.output.file"],
            SelectFileType.SaveFile, settings.invoiceOutputFilePath,
            listOf(FileChooser.ExtensionFilter(messages["create.invoice.window.pdf.extension.filter.description"], "*.pdf"))) { path ->
            settings.invoiceOutputFilePath = path
            presenter.saveSettings()
        }
    }


    private fun createNewTimeTrackerAccount() {
        timeTrackerAccountPresenter.showCreateTimeTrackerAccountWindow { createdTimeTrackerAccount ->
            createdTimeTrackerAccount?.let {
                showAvailableTimeTrackerAccounts()

                settingsViewModel.timeTrackerAccount.value = createdTimeTrackerAccount
            }
        }
    }

    private fun editSelectedTimeTrackerAccount() {
        timeTrackerAccountPresenter.showEditTimeTrackerAccountWindow(settingsViewModel.timeTrackerAccount.value) { didUserSaveTimeTrackerAccount ->
            if (didUserSaveTimeTrackerAccount) {
                showAvailableTimeTrackerAccounts()
            }
        }
    }

    private fun showAvailableTimeTrackerAccounts() {
        timeTrackerAccounts.setAll(timeTrackerAccountPresenter.getAllTimeTrackerAccounts())
    }

    private fun importTimeTrackerData() {
        timeTrackerAccountPresenter.importTimeTrackerDataAsync(settingsViewModel.timeTrackerAccount.value) { trackedTimes ->
            showTrackedMonths(trackedTimes)
        }
    }

    private fun showTrackedMonths(trackedTimes: TrackedTimes) {
        trackedMonths.setAll(trackedTimes.months.sortedByDescending { it.month })
    }


    private fun selectedTrackedMonthChanged(trackedMonth: TrackedMonth) {
        invoiceItemQuantity.value = trackedMonth.decimalHours

        invoiceViewModel.invoiceStartDate.value = trackedMonth.firstTrackedDay?.asLocalDate()
        invoiceViewModel.invoiceEndDate.value = trackedMonth.lastTrackedDay?.asLocalDate()

        updateInvoiceDescription()
    }

    private fun updateInvoiceDescription() {
        // TODO: check if user already set this value
        invoiceViewModel.invoiceDescription.value = settingsViewModel.clientName.value +
                (selectedTrackedMonth.item?.let { " " + MonthDateFormatter.format(it.month.asLocalDate()) } ?: "")
    }

    private fun updateInvoiceNumber() {
        // TODO: check if user already set this value
        try {
            invoiceViewModel.invoiceNumber.value = InvoiceDateFormatter.format(invoiceViewModel.invoicingDate.value)
        } catch (e: Exception) {
            logger.warn("Could not create invoice number from date ${invoiceViewModel.invoicingDate.value}", e)
        }
    }


    private fun createInvoice() {
        applySettingsChanges()

        val invoiceItems = listOf(
            presenter.createDocumentItem(0, invoiceItemQuantity.value)
        )

        val invoice = Document.createInvoice(invoiceItems, invoiceViewModel.invoiceNumber.value,
            invoiceViewModel.invoiceDescription.value, invoiceViewModel.invoicingDate.value.asUtilDate(), null,
            settings.lastSelectedRecipient)

        presenter.createAndShowInvoice(invoice)

        overviewPresenter.saveOrUpdate(invoice)

        closeWindow()
    }

    private fun applySettingsChanges() {
        settingsViewModel.commit()

        settings.timeTrackerAccount = settingsViewModel.timeTrackerAccount.value

        presenter.saveSettings()
    }

    private fun closeWindow() {
        this.close()
    }

}