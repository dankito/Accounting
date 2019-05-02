package net.dankito.accounting.data.model.banking

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.*


class BankAccountTransactionTest {

    companion object {
        private val ValueDate = Date()
    }


    @Test
    fun testEquals() {
        val transaction1 = createTransaction(BigDecimal("150.0"))
        val transaction2 = createTransaction(BigDecimal("150.00"))
        val transaction3 = createTransaction(BigDecimal("1.5E+2"))

        assertThat(transaction1.equals(transaction2)).isTrue()
        assertThat(transaction1.equals(transaction3)).isTrue()
        assertThat(transaction2.equals(transaction3)).isTrue()
    }

    @Test
    fun testHashCode() {
        val transaction1 = createTransaction(BigDecimal("150.0"))
        val transaction2 = createTransaction(BigDecimal("150.00"))
        val transaction3 = createTransaction(BigDecimal("1.5E+2"))

        assertThat(transaction1.hashCode()).isEqualTo(transaction2.hashCode())
        assertThat(transaction1.hashCode()).isEqualTo(transaction3.hashCode())
        assertThat(transaction2.hashCode()).isEqualTo(transaction3.hashCode())
    }


    private fun createTransaction(amount: BigDecimal): BankAccountTransaction {
        return BankAccountTransaction(amount, "", false, "", "", "", ValueDate, "", "", BigDecimal.ZERO, BankAccount())
    }

}