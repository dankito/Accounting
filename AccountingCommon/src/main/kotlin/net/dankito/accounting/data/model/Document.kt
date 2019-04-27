package net.dankito.accounting.data.model

import java.io.File
import java.util.*
import javax.persistence.*


/**
 * A document for tracking revenues and expenditures.
 *
 * (German: Beleg)
 */
@Entity
open class Document() : DocumentBase() {

    companion object {

        const val DocumentTypeColumnName = "type"

        const val IsSelfCreatedInvoiceColumnName = "is_self_created_invoice"

        const val IssueDateColumnName = "issue_date"

        const val DueDateColumnName = "due_day"

        const val PaymentDateColumnName = "payment_date"

        const val PaymentStateColumnName = "payment_state"

        const val DocumentNumberColumnName = "document_number"

        const val FilePathColumnName = "file_path"


        @JvmOverloads
        fun createInvoice(invoiceItems: List<DocumentItem>, documentNumber: String?,
                          issueDate: Date = Date(), dueDate: Date? = null): Document {

            val netAmount = invoiceItems.sumByDouble { it.netAmount }
            val valueAddedTaxRate = if (invoiceItems.isEmpty()) 0f else invoiceItems[0].valueAddedTaxRate
            val valueAddedTax = invoiceItems.sumByDouble { it.valueAddedTax }
            val totalAmount = invoiceItems.sumByDouble { it.totalAmount }

            return Document(DocumentType.Revenue, true, netAmount, valueAddedTaxRate, valueAddedTax, totalAmount,
                documentNumber, null, PaymentState.Outstanding, issueDate, dueDate, null, null, invoiceItems)
        }

    }


    constructor(type: DocumentType) : this() {
        this.type = type
    }

    constructor(type: DocumentType, totalAmount: Double, valueAddedTaxRate: Float,
                paymentState: PaymentState = PaymentState.Paid) : this(type) {

        this.valueAddedTaxRate = valueAddedTaxRate
        this.totalAmount = totalAmount
        this.paymentState = paymentState
    }

    constructor(type: DocumentType, isSelfCreatedInvoice: Boolean, netAmount: Double, valueAddedTaxRate: Float,
                valueAddedTax: Double, totalAmount: Double, documentNumber: String?, documentDescription: String?,
                paymentState: PaymentState, issueDate: Date?, dueDate: Date?, paymentDate: Date?, filePath: File?,
                items: List<DocumentItem> = listOf()
    ) : this(type, totalAmount, valueAddedTaxRate) {

        this.isSelfCreatedInvoice = isSelfCreatedInvoice
        this.netAmount = netAmount
        this.valueAddedTax = valueAddedTax
        this.documentNumber = documentNumber
        this.description = documentDescription
        this.paymentState = paymentState
        this.issueDate = issueDate
        this.dueDate = dueDate
        this.paymentDate = paymentDate
        this.filePath = filePath
        this.items = items
    }




    @Enumerated(EnumType.ORDINAL)
    @Column(name = DocumentTypeColumnName)
    var type: DocumentType = DocumentType.Expenditure

    @Column(name = IsSelfCreatedInvoiceColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    var isSelfCreatedInvoice: Boolean = false

    @Column(name = DocumentNumberColumnName)
    var documentNumber: String? = null

    @Enumerated(EnumType.ORDINAL)
    @Column(name = PaymentStateColumnName)
    var paymentState: PaymentState = PaymentState.Paid

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = IssueDateColumnName)
    var issueDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = DueDateColumnName)
    var dueDate: Date? = null

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = PaymentDateColumnName)
    var paymentDate: Date? = null

    @Column(name = FilePathColumnName)
    var filePath: File? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    var items: List<DocumentItem> = listOf()



    override fun toString(): String {
        return "$type of $totalAmount ($paymentState) for $description)"
    }


}