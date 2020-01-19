package net.dankito.accounting.javafx.windows.invoice.model

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import tornadofx.ViewModel
import tornadofx.observable


class CreateInvoiceSettingsViewModel(val settings: CreateInvoiceSettings) : ViewModel() {

    val timeTrackerAccount = SimpleObjectProperty<TimeTrackerAccount>(settings.timeTrackerAccount)

    val isATimeTrackerAccountSelected = SimpleBooleanProperty(timeTrackerAccount.value != null)


    val clientName = bind { settings.lastSelectedRecipient.observable(NaturalOrLegalPerson::name) }

    val clientAddressStreetName = bind { settings.lastSelectedRecipient.address.observable(Address::street) }

    val clientAddressStreetNumber = bind { settings.lastSelectedRecipient.address.observable(Address::streetNumber) }

    val clientAddressPostalCode = bind { settings.lastSelectedRecipient.address.observable(Address::zipCode) }

    val clientAddressCity = bind { settings.lastSelectedRecipient.address.observable(Address::city) }


    val invoiceItemUnitPrice = bind { settings.observable(CreateInvoiceSettings::invoiceItemUnitPrice) } as DoubleProperty

    val invoiceItemDescription = bind { settings.observable(CreateInvoiceSettings::invoiceItemDescription)}

    val valueAddedTax = bind { settings.observable(CreateInvoiceSettings::valueAddedTaxRate) } as DoubleProperty

}