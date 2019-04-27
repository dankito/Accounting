package net.dankito.accounting.service

import java.math.BigDecimal
import java.math.RoundingMode


open class ValueAddedTaxCalculator {

    companion object {
        private const val POSITIVE_ZERO = 0.0
    }


    // for currency calculations prefer BigDecimal as it's 10 based compared to 2 based Double

    open fun calculateVatFromTotalAmount(totalAmount: Double, vatRate: Float): Double {
        return calculateVatFromTotalAmount(toBigDecimal(totalAmount), vatRate).toDouble()
    }

    open fun calculateNetAmountFromTotalAmount(totalAmount: Double, vatRate: Float): Double {
        return calculateNetAmountFromTotalAmount(toBigDecimal(totalAmount), vatRate).toDouble()
    }

    open fun calculateVatFromTotalAmountRounded(totalAmount: Double, vatRate: Float): Double {
        return calculateVatFromTotalAmountRounded(toBigDecimal(totalAmount), vatRate).toDouble()
    }

    open fun calculateVatFromTotalAmount(totalAmount: BigDecimal, vatRate: Float): BigDecimal {
        return totalAmount - calculateNetAmountFromTotalAmount(totalAmount, vatRate)
    }

    open fun calculateNetAmountFromTotalAmount(totalAmount: BigDecimal, vatRate: Float): BigDecimal {
        val divisor = toBigDecimal(1 + ensurePercentageIsSmallerOne(vatRate))

        return totalAmount.divide(divisor, 5, RoundingMode.DOWN)
    }

    open fun calculateVatFromTotalAmountRounded(totalAmount: BigDecimal, vatRate: Float): BigDecimal {
        return roundToTwoDecimalPlaces(calculateVatFromTotalAmount(totalAmount, vatRate))
    }


    /**
     * [roundDownNetAmount] is needed for revenues in Germany where you first have to round down the net amount and
     * calculate VAT from this value.
     */
    @JvmOverloads
    open fun calculateVatFromNetAmount(netAmount: Double, vatRate: Float, roundDownNetAmount: Boolean = false): Double {
        return calculateVatFromNetAmount(toBigDecimal(netAmount), vatRate, roundDownNetAmount).toDouble()
    }

    /**
     * [roundDownNetAmount] is needed for revenues in Germany where you first have to round down the net amount and
     * calculate VAT from this value.
     */
    @JvmOverloads
    open fun calculateVatFromNetAmountRounded(netAmount: Double, vatRate: Float, roundDownNetAmount: Boolean = false): Double {
        return calculateVatFromNetAmountRounded(toBigDecimal(netAmount), vatRate, roundDownNetAmount).toDouble()
    }

    /**
     * [roundDownNetAmount] is needed for revenues in Germany where you first have to round down the net amount and
     * calculate VAT from this value.
     */
    @JvmOverloads
    open fun calculateVatFromNetAmount(netAmount: BigDecimal, vatRate: Float, roundDownNetAmount: Boolean = false): BigDecimal {
        val netAmountForCalculation = if (roundDownNetAmount) round(netAmount, 0, RoundingMode.DOWN) else netAmount

        return netAmountForCalculation * ensurePercentageIsSmallerOne(vatRate).toBigDecimal()
    }

    /**
     * [roundDownNetAmount] is needed for revenues in Germany where you first have to round down the net amount and
     * calculate VAT from this value.
     */
    @JvmOverloads
    open fun calculateVatFromNetAmountRounded(netAmount: BigDecimal, vatRate: Float, roundDownNetAmount: Boolean = false): BigDecimal {
        return roundToTwoDecimalPlaces(calculateVatFromNetAmount(netAmount, vatRate, roundDownNetAmount))
    }


    protected open fun ensurePercentageIsSmallerOne(percentage: Float): Float {
        if (percentage > 1f) {
            return percentage / 100f
        }

        return percentage
    }


    open fun roundToTwoDecimalPlaces(double: Double): Double {
        return round(double, 2)
    }

    // from Apache commons-match (https://github.com/apache/commons-math/blob/3.6-release/src/main/java/org/apache/commons/math3/util/Precision.java)
    @JvmOverloads
    open fun round(double: Double, scale: Int, roundingMode: RoundingMode = RoundingMode.DOWN): Double {
        try {
            val rounded = toBigDecimal(double)
                .setScale(scale, roundingMode)
                .toDouble()

            // MATH-1089: negative values rounded to zero should result in negative zero
            return if (rounded == POSITIVE_ZERO) POSITIVE_ZERO * double else rounded
        } catch (ex: NumberFormatException) {
            return if (java.lang.Double.isInfinite(double)) double else Double.NaN
        }
    }

    open fun roundToTwoDecimalPlaces(double: BigDecimal): BigDecimal {
        return round(double, 2)
    }

    @JvmOverloads
    open fun round(double: BigDecimal, scale: Int, roundingMode: RoundingMode = RoundingMode.DOWN): BigDecimal {
        try {
            val rounded = double
                .setScale(scale, roundingMode)

            // MATH-1089: negative values rounded to zero should result in negative zero
//            return if (rounded == POSITIVE_ZERO) POSITIVE_ZERO * double else rounded
            return rounded
        } catch (ex: NumberFormatException) {
//            return if (java.lang.Double.isInfinite(double.toDouble())) double else Double.NaN
            return double
        }
    }


    protected open fun toBigDecimal(float: Float) = toBigDecimal(float.toDouble())

    protected open fun toBigDecimal(double: Double) = BigDecimal.valueOf(double)

}