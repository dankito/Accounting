package net.dankito.accounting.data.model.invoice


open class FormattedInvoiceItem(val index: Int = 1,
                                val description: String,
                                val unitPrice: String,
                                val quantity: String,
                                val netAmount: String

) {

    override fun toString(): String {
        return "[$index] $quantity à $unitPrice for $description"
    }

}