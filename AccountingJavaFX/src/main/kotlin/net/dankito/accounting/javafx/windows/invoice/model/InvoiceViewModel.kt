package net.dankito.accounting.javafx.windows.invoice.model

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.ViewModel
import java.time.LocalDate


class InvoiceViewModel : ViewModel() {

    val invoiceDescription = SimpleStringProperty()

    val invoicingDate = SimpleObjectProperty<LocalDate>(LocalDate.now())

    val invoiceNumber = SimpleStringProperty()

    val invoiceStartDate = SimpleObjectProperty<LocalDate>()

    val invoiceEndDate = SimpleObjectProperty<LocalDate>()

}