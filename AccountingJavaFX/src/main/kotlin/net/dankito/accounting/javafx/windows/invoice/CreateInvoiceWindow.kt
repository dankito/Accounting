package net.dankito.accounting.javafx.windows.invoice

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.stage.FileChooser
import javafx.util.converter.NumberStringConverter
import javafx.util.converter.PercentageStringConverter
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.CreateInvoicePresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.presenter.TimeTrackerAccountPresenter
import net.dankito.accounting.javafx.windows.invoice.controls.SelectFileView
import net.dankito.accounting.javafx.windows.invoice.model.CreateInvoiceSettingsViewModel
import net.dankito.accounting.javafx.windows.invoice.model.InvoiceViewModel
import net.dankito.accounting.javafx.windows.invoice.model.SelectFileType
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*
import javax.inject.Inject


class CreateInvoiceWindow : Window() {

    companion object {

        private const val TimeTrackerAccountButtonsWidth = 110.0

        private const val ButtonWidth = 150.0

        private const val ButtonHeight = 40.0

        private const val ButtonBottomAnchor = 24.0

    }


    override val root = Form()


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


    private var selectInvoiceTemplateFileView: SelectFileView by singleAssign()

    private var selectInvoiceOutputFileView: SelectFileView by singleAssign()


    init {
        AppComponent.component.inject(this)

        settings = presenter.settings

        settingsViewModel = CreateInvoiceSettingsViewModel(settings)

        showAvailableTimeTrackerAccounts()

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

                    hboxConstraints {
                        marginLeft = 12.0
                    }
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

            field(messages["create.invoice.window.invoice.value.added.tax"]) {
                textfield().bind(settingsViewModel.valueAddedTax, false, PercentageStringConverter())
            }
        }

        fieldset(messages["create.invoice.window.invoice.items.settings"]) {
            field(messages["create.invoice.window.invoice.item.description"]) {
                textfield(settingsViewModel.invoiceItemDescription) {
                    this.promptText = messages["create.invoice.window.invoice.item.description.prompt.text"]
                }
            }

            field(messages["create.invoice.window.invoice.item.unit.price"]) {
                textfield().bind(settingsViewModel.invoiceItemUnitPrice, false, NumberStringConverter())
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


    private fun createInvoice() {
        applySettingsChanges()

        val invoiceItems = listOf(
            presenter.createDocumentItem(0, 80.0) // TODO: set quantity
        )

        val invoice = Document.createInvoice(invoiceItems, invoiceViewModel.invoiceNumber.value,
            invoiceViewModel.invoicingDate.value.asUtilDate(), null, settings.lastSelectedRecipient)

        presenter.createAndShowInvoice(invoice)

        overviewPresenter.saveOrUpdate(invoice)

        closeWindow()
    }

    private fun applySettingsChanges() {
        settingsViewModel.commit()

        presenter.saveSettings()
    }

    private fun closeWindow() {
        this.close()
    }

}