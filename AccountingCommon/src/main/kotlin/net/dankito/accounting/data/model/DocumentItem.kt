package net.dankito.accounting.data.model

import javax.persistence.Column
import javax.persistence.Entity


@Entity
open class DocumentItem protected constructor() : BaseEntity() {

    companion object {

        val UnsetAmount = Double.NaN

        val UnsetVatRate = Float.NaN

        const val UnsetIndex = -1


        const val IndexColumnName = "index"

        const val DocumentItemDescriptionColumnName = "description"

        const val UnitPriceColumnName = "unit_price"

        const val QuantityColumnName = "quantity"

        const val NetAmountColumnName = "net_amount"

        const val ValueAddedTaxRateColumnName = "value_added_tax_rate"

        const val ValueAddedTaxColumnName = "value_added_tax"

        const val GrossAmountColumnName = "total_amount" // TODO: rename to gross_amount

    }


    constructor(valueAddedTaxRate: Float, grossAmount: Double, description: String? = null)
            : this(UnsetAmount, valueAddedTaxRate, UnsetAmount, grossAmount, description)

    constructor(netAmount: Double, valueAddedTaxRate: Float, valueAddedTax: Double, grossAmount: Double,
                description: String? = null) : this() {

        this.netAmount = netAmount
        this.valueAddedTaxRate = valueAddedTaxRate
        this.valueAddedTax = valueAddedTax
        this.grossAmount = grossAmount
        this.description = description
    }

    constructor(index: Int, description: String?, unitPrice: Double, quantity: Double,
                netAmount: Double, valueAddedTaxRate: Float, valueAddedTax: Double, grossAmount: Double)
            : this(netAmount, valueAddedTaxRate, valueAddedTax, grossAmount, description) {

        this.index = index
        this.unitPrice = unitPrice
        this.quantity = quantity
    }


    @Column(name = DocumentItemDescriptionColumnName)
    var description: String? = null

    @Column(name = NetAmountColumnName)
    var netAmount: Double = UnsetAmount

    @Column(name = ValueAddedTaxRateColumnName)
    var valueAddedTaxRate: Float = UnsetVatRate

    @Column(name = ValueAddedTaxColumnName)
    var valueAddedTax: Double = UnsetAmount

    @Column(name = GrossAmountColumnName)
    var grossAmount: Double = UnsetAmount


    @Column(name = IndexColumnName)
    var index: Int = UnsetIndex

    @Column(name = UnitPriceColumnName)
    var unitPrice: Double = 0.0

    @Column(name = QuantityColumnName)
    var quantity: Double = 0.0


    val isNetAmountSet: Boolean
        get() = netAmount.isNaN() == false // != UnsetAmount

    val isGrossAmountSet: Boolean
        get() = grossAmount.isNaN() == false

    val isValueAddedTaxSet: Boolean
        get() = valueAddedTax.isNaN() == false

    val isValueAddedTaxRateSet: Boolean
        get() = valueAddedTaxRate.isNaN() == false // != UnsetVatRate does not work


    protected open fun ensureVatRateIsBetweenZeroAndOne(valueAddedTaxRate: Float): Float {
        if (valueAddedTaxRate > 1f) {
            return valueAddedTaxRate / 100f
        }

        return valueAddedTaxRate
    }


    override fun toString(): String {
        if (index > UnsetIndex) {
            return "[$index] $quantity à $unitPrice for $description"
        }

        return super.toString()
    }

}