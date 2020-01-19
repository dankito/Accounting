package net.dankito.accounting.data.model

import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.filter.EntityFilter
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import java.util.*
import javax.persistence.*


/**
 * A document for tracking revenues and expenditures.
 *
 * (German: Beleg)
 */
@Entity
open class Document() : BaseEntity() {

    companion object {

        const val DocumentTypeColumnName = "type"

        const val DocumentDescriptionColumnName = "document_description"

        const val IsSelfCreatedInvoiceColumnName = "is_self_created_invoice"

        const val IssueDateColumnName = "issue_date"

        const val DueDateColumnName = "due_day"

        const val PaymentDateColumnName = "payment_date"

        const val PaymentStateColumnName = "payment_state"

        const val DocumentNumberColumnName = "document_number"

        const val FilePathColumnName = "file_path"

        const val IssuerJoinColumnName = "issuer"

        const val RecipientJoinColumnName = "recipient"

        const val CreatedFromDocumentJoinColumnName = "created_from_document"

        const val AutomaticallyCreatedFromFilterJoinColumnName = "automatically_created_from_filter"


        @JvmOverloads
        fun createInvoice(invoiceItems: List<DocumentItem>, documentNumber: String?, documentDescription: String? = null,
                          issueDate: Date = Date(), dueDate: Date? = null, recipient: NaturalOrLegalPerson? = null): Document {

            return Document(DocumentType.Revenue, invoiceItems, PaymentState.Outstanding, true, documentNumber,
                documentDescription, issueDate, dueDate, null, null, null, recipient)
        }

    }


    constructor(type: DocumentType, totalAmount: Double, valueAddedTaxRate: Float, paymentState: PaymentState = PaymentState.Paid)
            : this(type, listOf(DocumentItem(valueAddedTaxRate, totalAmount)), paymentState)

    constructor(type: DocumentType, items: List<DocumentItem> = listOf(), paymentState: PaymentState = PaymentState.Paid,
                isSelfCreatedInvoice: Boolean = false, documentNumber: String? = null, documentDescription: String? = null,
                issueDate: Date? = null, dueDate: Date? = null, paymentDate: Date? = null, filePath: String? = null,
                issuer: NaturalOrLegalPerson? = null, recipient: NaturalOrLegalPerson? = null)
            : this() {

        this.type = type
        this.items = items
        this.paymentState = paymentState
        this.isSelfCreatedInvoice = isSelfCreatedInvoice
        this.documentNumber = documentNumber
        this.description = documentDescription
        this.paymentState = paymentState
        this.issueDate = issueDate
        this.dueDate = dueDate
        this.paymentDate = paymentDate
        this.filePath = filePath
        this.items = items
        this.issuer = issuer
        this.recipient = recipient
    }




    @Enumerated(EnumType.ORDINAL)
    @Column(name = DocumentTypeColumnName)
    var type: DocumentType = DocumentType.Expenditure

    @Column(name = DocumentDescriptionColumnName)
    var description: String? = null

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
    var filePath: String? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    var items: List<DocumentItem> = mutableListOf()

    @OneToOne(fetch = FetchType.LAZY, cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name = IssuerJoinColumnName)
    var issuer: NaturalOrLegalPerson? = null

    @OneToOne(fetch = FetchType.LAZY, cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name = RecipientJoinColumnName)
    var recipient: NaturalOrLegalPerson? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CreatedFromDocumentJoinColumnName)
    var createdFromAccountTransaction: BankAccountTransaction? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = AutomaticallyCreatedFromFilterJoinColumnName)
    var automaticallyCreatedFromFilter: EntityFilter? = null


    val netAmount: Double
        get() = items.sumByDouble { it.netAmount }

    val valueAddedTaxRates: List<Float>
        get() = items.map { it.valueAddedTaxRate }

    val isValueAddedTaxRateSet: Boolean
        get() = valueAddedTaxRates.isNotEmpty()

    val valueAddedTax: Double
        get() = items.sumByDouble { it.valueAddedTax }

    val totalAmount: Double
        get() = items.sumByDouble { it.grossAmount }



    open fun hasVatForVatRate(vatRate: Float): Boolean {
        return valueAddedTaxRates.contains(vatRate)
    }

    open fun getVatForVatRate(vatRate: Float): Double? {
        return items.filter { it.valueAddedTaxRate == vatRate }.sumByDouble { it.valueAddedTax }
    }


    open fun addItem(item: DocumentItem): Boolean {
        if (items !is MutableList) {
            items = ArrayList(items)
        }

        (items as? MutableList)?.let { mutableItems ->
            return mutableItems.add(item)
        }

        return false
    }

    open fun removeItem(item: DocumentItem): Boolean {
        (items as? MutableList)?.let { mutableItems ->
            return mutableItems.remove(item)
        }

        return false
    }


    override fun toString(): String {
        return "$type of $totalAmount (vatRates = $valueAddedTaxRates, vat = $valueAddedTax, net = $netAmount) for $description)"
    }

}