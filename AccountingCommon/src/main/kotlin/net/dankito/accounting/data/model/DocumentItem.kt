package net.dankito.accounting.data.model

import javax.persistence.Column
import javax.persistence.Entity


@Entity
open class DocumentItem() : DocumentBase() {

    companion object {

        const val UnsetIndex = -1


        const val IndexColumnName = "index"

        const val UnitPriceColumnName = "unit_price"

        const val QuantityColumnName = "quantity"

    }


    constructor(netAmount: Double, valueAddedTaxRate: Float, valueAddedTax: Double, totalAmount: Double,
                description: String?) : this() {

        this.netAmount = netAmount
        this.valueAddedTaxRate = valueAddedTaxRate
        this.valueAddedTax = valueAddedTax
        this.totalAmount = totalAmount
        this.description = description
    }

    constructor(index: Int, description: String?, unitPrice: Double, quantity: Double,
                netAmount: Double, valueAddedTaxRate: Float, valueAddedTax: Double, totalAmount: Double)
            : this(netAmount, valueAddedTaxRate, valueAddedTax, totalAmount, description) {

        this.index = index
        this.unitPrice = unitPrice
        this.quantity = quantity
    }


    @Column(name = IndexColumnName)
    var index: Int = UnsetIndex

    @Column(name = UnitPriceColumnName)
    var unitPrice: Double = 0.0

    @Column(name = QuantityColumnName)
    var quantity: Double = 0.0


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