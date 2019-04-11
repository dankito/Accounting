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
        return totalAmount / toBigDecimal(1 + ensurePercentageIsSmallerZero(vatRate))
    }

    open fun calculateVatFromTotalAmountRounded(totalAmount: BigDecimal, vatRate: Float): BigDecimal {
        return roundToTwoDecimalPlaces(calculateVatFromTotalAmount(totalAmount, vatRate))
    }


    open fun calculateVatFromNetAmount(netAmount: Double, vatRate: Float): Double {
        return calculateVatFromNetAmount(toBigDecimal(netAmount), vatRate).toDouble()
    }

    open fun calculateVatFromNetAmountRounded(netAmount: Double, vatRate: Float): Double {
        return calculateVatFromNetAmountRounded(toBigDecimal(netAmount), vatRate).toDouble()
    }

    open fun calculateVatFromNetAmount(netAmount: BigDecimal, vatRate: Float): BigDecimal {
        return netAmount * ensurePercentageIsSmallerZero(vatRate).toBigDecimal()
    }

    open fun calculateVatFromNetAmountRounded(netAmount: BigDecimal, vatRate: Float): BigDecimal {
        return roundToTwoDecimalPlaces(calculateVatFromNetAmount(netAmount, vatRate))
    }


    protected open fun ensurePercentageIsSmallerZero(percentage: Float): Float {
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

    protected open fun toBigDecimal(double: Double) = BigDecimal(double)

}