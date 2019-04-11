package net.dankito.accounting.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.Test
import java.math.RoundingMode

class ValueAddedTaxCalculatorTest {

    companion object {
        private const val GermanDefaultVatRate = 19f
    }


    private val underTest = ValueAddedTaxCalculator()


    @Test
    fun calculateVatFromTotalAmount_11_99_to_1_91() {

        // when
        val result = underTest.calculateVatFromTotalAmount(11.99, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(1.91, within(0.01))
    }

    @Test
    fun calculateVatFromTotalAmountRounded_11_99_to_1_91() {

        // when
        val result = underTest.calculateVatFromTotalAmountRounded(11.99, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(1.91)
    }

    @Test
    fun calculateVatFromNetAmount_10_08_to_1_91() {

        // when
        val result = underTest.calculateVatFromNetAmount(10.08, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(1.91, within(0.01))
    }

    @Test
    fun calculateVatFromNetAmountRounded_10_08_to_1_91() {

        // when
        val result = underTest.calculateVatFromNetAmountRounded(10.08, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(1.91)
    }


    @Test
    fun calculateVatFromTotalAmount_836_27_to_133_52() {

        // when
        val result = underTest.calculateVatFromTotalAmount(836.27, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(133.52, within(0.01))
    }

    @Test
    fun calculateVatFromTotalAmountRounded_836_27_to_133_52() {

        // when
        val result = underTest.calculateVatFromTotalAmountRounded(836.27, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(133.52)
    }

    @Test
    fun calculateVatFromNetAmount_702_75_to_133_52() {

        // when
        val result = underTest.calculateVatFromNetAmount(702.75, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(133.52, within(0.01))
    }

    @Test
    fun calculateVatFromNetAmountRounded_702_75_to_133_52() {

        // when
        val result = underTest.calculateVatFromNetAmountRounded(702.75, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(133.52)
    }


    @Test
    fun calculateVatFromTotalAmount_236_81_to_37_81() {

        // when
        val result = underTest.calculateVatFromTotalAmount(236.81, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(37.81, within(0.01))
    }

    @Test
    fun calculateVatFromTotalAmountRounded_236_81_to_37_81() {

        // when
        val result = underTest.calculateVatFromTotalAmountRounded(236.81, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(37.81)
    }

    @Test
    fun calculateVatFromNetAmount_199_00_to_37_81() {

        // when
        val result = underTest.calculateVatFromNetAmount(199.0, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(37.81, within(0.01))
    }

    @Test
    fun calculateVatFromNetAmountRounded_199_00_to_37_81() {

        // when
        val result = underTest.calculateVatFromNetAmountRounded(199.0, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(37.81)
    }


    // this here are values calculated by ELSTER

    @Test
    fun calculateVatFromNetAmount_10903_20_to_2071_57() {

        // given: for ELSTER we have to round net amount down
        val downRounded = underTest.round(10903.20, 0, RoundingMode.DOWN)

        // when
        val result = underTest.calculateVatFromNetAmount(downRounded, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2071.57, within(0.01))
    }

    @Test
    fun calculateVatFromNetAmountRounded_10903_20_to_2071_57() {

        // given: for ELSTER we have to round net amount down
        val downRounded = underTest.round(10903.20, 0, RoundingMode.DOWN)

        // when
        val result = underTest.calculateVatFromNetAmountRounded(downRounded, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2071.57)
    }

    @Test
    fun calculateVatFromNetAmount_11048_00_to_2099_12() {

        // when
        val result = underTest.calculateVatFromNetAmount(11048.00, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2099.12, within(0.01))
    }

    @Test
    fun calculateVatFromNetAmountRounded_11048_00_to_2099_12() {

        // when
        val result = underTest.calculateVatFromNetAmountRounded(11048.00, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2099.12)
    }

    @Test
    fun calculateVatFromNetAmount_10912_80_to_2073_28() {

        // given: for ELSTER we have to round net amount down
        val downRounded = underTest.round(10912.80, 0, RoundingMode.DOWN)

        // when
        val result = underTest.calculateVatFromNetAmount(downRounded, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2073.28, within(0.01))
    }

    @Test
    fun calculateVatFromNetAmountRounded_10912_80_to_2073_28() {

        // given: for ELSTER we have to round net amount down
        val downRounded = underTest.round(10912.80, 0, RoundingMode.DOWN)

        // when
        val result = underTest.calculateVatFromNetAmountRounded(downRounded, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2073.28)
    }


    // ELSTER values reverse calculation

    @Test
    fun calculateVatFromTotalAmount_12974_57_to_2071_57() {

        // when
        val result = underTest.calculateVatFromTotalAmount(12974.57, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2071.57, within(0.01))
    }

    @Test
    fun calculateVatFromTotalAmountRounded_12974_57_to_2071_57() {

        // when
        val result = underTest.calculateVatFromTotalAmountRounded(12974.57, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2071.57)
    }

    @Test
    fun calculateVatFromTotalAmount_13147_12_to_2099_12() {

        // when
        val result = underTest.calculateVatFromTotalAmount(13147.12, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2099.12, within(0.01))
    }

    @Test
    fun calculateVatFromTotalAmountRounded_13147_12_to_2099_12() {

        // when
        val result = underTest.calculateVatFromTotalAmountRounded(13147.12, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2099.12)
    }

    @Test
    fun calculateVatFromTotalAmount_12985_28_to_2073_28() {

        // when
        val result = underTest.calculateVatFromTotalAmount(12985.28, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2073.28, within(0.01))
    }

    @Test
    fun calculateVatFromTotalAmountRounded_12985_28_to_2073_28() {

        // when
        val result = underTest.calculateVatFromTotalAmountRounded(12985.28, GermanDefaultVatRate)

        // then
        assertThat(result).isEqualTo(2073.28)
    }

}