package net.dankito.accounting.data.model

import java.io.File
import java.util.*
import javax.persistence.*


/**
 * A document for tracking revenues and expenditures.
 *
 * (German: Beleg)
 */
open class Document() : BaseEntity() {

    companion object {

        val UnsetAmount = Double.NaN

        val UnsetVatRate = Float.NaN


        const val DocumentTypeColumnName = "type"

        const val NetAmountColumnName = "net_amount"

        const val ValueAddedTaxRateColumnName = "value_added_tax_rate"

        const val ValueAddedTaxColumnName = "value_added_tax"

        const val TotalAmountColumnName = "total_amount"

        const val IsSelfCreatedInvoiceColumnName = "is_self_created_invoice"

        const val IssueDateColumnName = "issue_date"

        const val DueDateColumnName = "due_day"

        const val PaymentDateColumnName = "payment_date"

        const val PaymentStateColumnName = "payment_state"

        const val DocumentNumberColumnName = "document_number"

        const val DocumentDescriptionColumnName = "document_description"

        const val FilePathColumnName = "file_path"


        @JvmOverloads
        fun createInvoice(netAmount: Double, valueAddedTaxRate: Float, valueAddedTax: Double, totalAmount: Double,
                          documentNumber: String?, documentDescription: String?,
                          issueDate: Date = Date(), dueDate: Date? = null, filePath: File? = null): Document {

            return Document(DocumentType.Revenue, true, netAmount, valueAddedTaxRate, valueAddedTax, totalAmount,
                documentNumber, documentDescription, PaymentState.Outstanding, issueDate, dueDate, null, filePath)
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
                paymentState: PaymentState, issueDate: Date?, dueDate: Date?, paymentDate: Date?, filePath: File?
    ) : this(type, totalAmount, valueAddedTaxRate) {

        this.isSelfCreatedInvoice = isSelfCreatedInvoice
        this.netAmount = netAmount
        this.valueAddedTax = valueAddedTax
        this.documentNumber = documentNumber
        this.documentDescription = documentDescription
        this.paymentState = paymentState
        this.issueDate = issueDate
        this.dueDate = dueDate
        this.paymentDate = paymentDate
        this.filePath = filePath
    }




    @Enumerated(EnumType.ORDINAL)
    @Column(name = DocumentTypeColumnName)
    var type: DocumentType = DocumentType.Expenditure

    @Column(name = IsSelfCreatedInvoiceColumnName, columnDefinition = "SMALLINT DEFAULT 0", nullable = false)
    var isSelfCreatedInvoice: Boolean = false

    @Column(name = NetAmountColumnName)
    var netAmount: Double = UnsetAmount

    @Column(name = ValueAddedTaxRateColumnName)
    var valueAddedTaxRate: Float = UnsetVatRate

    @Column(name = ValueAddedTaxColumnName)
    var valueAddedTax: Double = UnsetAmount

    @Column(name = TotalAmountColumnName)
    var totalAmount: Double = UnsetAmount

    @Column(name = DocumentNumberColumnName)
    var documentNumber: String? = null

    @Column(name = DocumentDescriptionColumnName)
    var documentDescription: String? = null

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



    val isNetAmountSet: Boolean
        @Transient
        get() = netAmount.isNaN() == false // != UnsetAmount

    val isTotalAmountSet: Boolean
        @Transient
        get() = totalAmount.isNaN() == false

    val isValueAddedTaxSet: Boolean
        @Transient
        get() = valueAddedTax.isNaN() == false

    val isValueAddedTaxRateSet: Boolean
        @Transient
        get() = valueAddedTaxRate.isNaN() == false // != UnsetVatRate does not work


    override fun toString(): String {
        return "$type of $totalAmount ($paymentState) for $documentDescription)"
    }


}