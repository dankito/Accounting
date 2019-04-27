package net.dankito.accounting.data.model.invoice

import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.BaseEntity
import net.dankito.accounting.data.model.Company
import net.dankito.accounting.data.model.NaturalOrLegalPerson
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToOne


@Entity
class CreateInvoiceSettings(

    @Column(name = InvoiceTemplateFilePathColumnName)
    var invoiceTemplateFilePath: String = "",

    @Column(name = InvoiceOutputFilePathColumnName)
    var invoiceOutputFilePath: String = "",

    @Column(name = InvoiceItemUnitPriceColumnName)
    var invoiceItemUnitPrice: Double = 0.0,

    @Column(name = InvoiceItemDescriptionColumnName)
    var invoiceItemDescription: String = "",

    @Column(name = ValueAddedTaxRateColumnName)
    var valueAddedTaxRate: Double = 0.19,

    @OneToOne
    @JoinColumn(name = LastSelectedRecipientJoinColumnName)
    var lastSelectedRecipient: NaturalOrLegalPerson = Company("", Address())

) : BaseEntity() {

    companion object {

        const val InvoiceTemplateFilePathColumnName = "invoice_template_file_path"

        const val InvoiceOutputFilePathColumnName = "invoice_output_file_path"

        const val InvoiceItemUnitPriceColumnName = "invoice_item_unitPrice"

        const val InvoiceItemDescriptionColumnName = "invoice_item_description"

        const val ValueAddedTaxRateColumnName = "value_added_tax_rate"

        const val LastSelectedRecipientJoinColumnName = "last_selected_recipient"

    }
}