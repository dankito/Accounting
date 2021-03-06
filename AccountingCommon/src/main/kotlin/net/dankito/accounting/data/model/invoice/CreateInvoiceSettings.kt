package net.dankito.accounting.data.model.invoice

import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.BaseEntity
import net.dankito.accounting.data.model.person.Company
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne


@Entity
class CreateInvoiceSettings(

    @OneToOne
    @JoinColumn(name = TimeTrackerAccountJoinColumnName)
    var timeTrackerAccount: TimeTrackerAccount? = null,

    @Column(name = InvoiceTemplateFilePathColumnName)
    var invoiceTemplateFilePath: String = "",

    @Column(name = InvoiceOutputFilePathColumnName)
    var invoiceOutputFilePath: String = "",

    @Column(name = InvoiceItemUnitPriceColumnName)
    var invoiceItemUnitPrice: Double = 0.0,

    @Column(name = InvoiceItemDescriptionColumnName)
    var invoiceItemDescription: String = "",

    @Column(name = ValueAddedTaxRateColumnName)
    var valueAddedTaxRate: Double = 19.0,

    @Column(name = TimeForPaymentColumnName)
    var timeForPayment: Int = 30,

    @OneToOne
    @JoinColumn(name = LastSelectedRecipientJoinColumnName)
    var lastSelectedRecipient: NaturalOrLegalPerson = Company("", PersonType.Client, Address())

) : BaseEntity() {

    companion object {

        const val TimeTrackerAccountJoinColumnName = "time_tracker_account"

        const val InvoiceTemplateFilePathColumnName = "invoice_template_file_path"

        const val InvoiceOutputFilePathColumnName = "invoice_output_file_path"

        const val InvoiceItemUnitPriceColumnName = "invoice_item_unitPrice"

        const val InvoiceItemDescriptionColumnName = "invoice_item_description"

        const val ValueAddedTaxRateColumnName = "value_added_tax_rate"

        const val TimeForPaymentColumnName = "time_for_payment"

        const val LastSelectedRecipientJoinColumnName = "last_selected_recipient"

    }
}