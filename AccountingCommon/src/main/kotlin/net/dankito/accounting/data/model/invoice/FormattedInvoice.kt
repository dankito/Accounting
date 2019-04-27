package net.dankito.accounting.data.model.invoice

import net.dankito.accounting.data.model.NaturalOrLegalPerson


class FormattedInvoice(val items: List<FormattedInvoiceItem>,
                       val invoicingDate: String,
                       val invoiceNumber: String,
                       val netAmount: String,
                       val valueAddedTaxRate: String,
                       val valueAddedTax: String,
                       val totalAmount: String,
                       val invoiceStartDate: String,
                       val invoiceEndDate: String,
                       val recipient: NaturalOrLegalPerson?
)