package net.dankito.accounting.service.banking

import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.GetAccountTransactionsResult
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference


@Ignore // not an automatic test, set your bank account credentials in getTestBankAccount()
class HbciBankingClientTest {

    private val underTest = HbciBankingClient()


    @Test
    fun getAccountTransactionsAsync() {
        val result = AtomicReference<GetAccountTransactionsResult>(null)
        val countDownLatch = CountDownLatch(1)

        underTest.getAccountTransactionsAsync(getTestBankAccount()) { getAccountTransactionsResult ->
            result.set(getAccountTransactionsResult)
            countDownLatch.countDown()
        }

        try { countDownLatch.await(40, TimeUnit.SECONDS) } catch (ignored: Exception) { }

        assertThat(result.get()).isNotNull
        assertThat(result.get().transactions?.transactions).isNotEmpty
    }


    private fun getTestBankAccount(): BankAccount {
        return BankAccount("", "", "")
    }

}