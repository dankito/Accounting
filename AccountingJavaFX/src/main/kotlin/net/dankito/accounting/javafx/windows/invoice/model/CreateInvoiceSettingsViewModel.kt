package net.dankito.accounting.javafx.windows.invoice.model

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import tornadofx.ViewModel
import tornadofx.observable


class CreateInvoiceSettingsViewModel(val settings: CreateInvoiceSettings) : ViewModel() {

    val timeTrackerAccount = SimpleObjectProperty<TimeTrackerAccount>(settings.timeTrackerAccount)

    val isATimeTrackerAccountSelected = SimpleBooleanProperty(timeTrackerAccount.value != null)


    val client = bind { settings.observable(CreateInvoiceSettings::lastSelectedRecipient) }


    val invoiceItemUnitPrice = bind { settings.observable(CreateInvoiceSettings::invoiceItemUnitPrice) } as DoubleProperty

    val invoiceItemDescription = bind { settings.observable(CreateInvoiceSettings::invoiceItemDescription)}

    val valueAddedTax = bind { settings.observable(CreateInvoiceSettings::valueAddedTaxRate) } as DoubleProperty

}