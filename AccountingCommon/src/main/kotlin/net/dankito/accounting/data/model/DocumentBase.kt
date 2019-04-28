package net.dankito.accounting.data.model

import javax.persistence.Column
import javax.persistence.MappedSuperclass


/**
 * Base class for [Document] and [DocumentItem] for an individual net amount, total amount, VAT, VAT rate and description.
 */
@MappedSuperclass
open class DocumentBase() : BaseEntity() {

    companion object {

        val UnsetAmount = Double.NaN

        val UnsetVatRate = Float.NaN


        const val NetAmountColumnName = "net_amount"

        const val ValueAddedTaxRateColumnName = "value_added_tax_rate"

        const val ValueAddedTaxColumnName = "value_added_tax"

        const val TotalAmountColumnName = "total_amount"

        const val DocumentDescriptionColumnName = "document_description"

    }


    constructor(netAmount: Double, valueAddedTaxRate: Float, valueAddedTax: Double, totalAmount: Double,
                documentDescription: String?) : this() {

        this.netAmount = netAmount
        this.valueAddedTaxRate = valueAddedTaxRate
        this.valueAddedTax = valueAddedTax
        this.totalAmount = totalAmount
        this.description = documentDescription
    }



    @Column(name = NetAmountColumnName)
    var netAmount: Double = UnsetAmount

    @Column(name = ValueAddedTaxRateColumnName)
    var valueAddedTaxRate: Float = UnsetVatRate

    @Column(name = ValueAddedTaxColumnName)
    var valueAddedTax: Double = UnsetAmount

    @Column(name = TotalAmountColumnName)
    var totalAmount: Double = UnsetAmount

    @Column(name = DocumentDescriptionColumnName)
    var description: String? = null



    val isNetAmountSet: Boolean
        get() = netAmount.isNaN() == false // != UnsetAmount

    val isTotalAmountSet: Boolean
        get() = totalAmount.isNaN() == false

    val isValueAddedTaxSet: Boolean
        get() = valueAddedTax.isNaN() == false

    val isValueAddedTaxRateSet: Boolean
        get() = valueAddedTaxRate.isNaN() == false // != UnsetVatRate does not work



    override fun toString(): String {
        return "$totalAmount (vat = $valueAddedTax, net = $netAmount) for $description)"
    }

}