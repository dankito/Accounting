package net.dankito.accounting.service.banking

import com.nhaarman.mockito_kotlin.doReturn
import net.dankito.accounting.data.dao.banking.IBankAccountDao
import net.dankito.accounting.data.dao.banking.IBankAccountTransactionDao
import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.person.Company
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.utils.events.RxEventBus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.util.*

class BankAccountServiceTest {

    companion object {

        private const val InvoiceTotalAmount = 1234.56

        private const val AnyOtherAmount = 6543.21

        private const val InvoiceRecipientName = "Abzock GmbH & Co KG"

        private const val InvoiceNumber = "1903271"

    }


    private val bankingClientMock = mock(IBankingClient::class.java)

    private val bankAccountDaoMock = mock(IBankAccountDao::class.java)

    private val transactionDaoMock = mock(IBankAccountTransactionDao::class.java)


    private val underTest = BankAccountService(bankingClientMock, bankAccountDaoMock, transactionDaoMock, RxEventBus())


    @Test
    fun isInvoicePaid_AmountAndInvoiceNumberMatches() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction(InvoiceTotalAmount, "noise $InvoiceNumber noise", "")))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNotNull
    }

    @Test
    fun isInvoicePaid_AmountDiffersByLessThan1PerThousandAndInvoiceNumberMatches() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction(InvoiceTotalAmount - 1, "noise $InvoiceNumber noise", "")))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNotNull
    }

    @Test
    fun isInvoicePaid_AmountAndRecipientMatches() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction(InvoiceTotalAmount, "noise", InvoiceRecipientName)))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNotNull
    }


    @Test
    fun isInvoicePaid_InvoiceNumberMatchesButNotAmount() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction(AnyOtherAmount, "noise $InvoiceNumber noise", "")))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNull()
    }

    @Test
    fun isInvoicePaid_RecipientMatchesButNotAmount() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction(AnyOtherAmount, "noise", InvoiceRecipientName)))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNull()
    }

    @Test
    fun isInvoicePaid_AmountMatchesButNeitherInvoiceNumberOrRecipient() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction(InvoiceTotalAmount, "noise", "noise")))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNull()
    }

    @Test
    fun isInvoicePaid_RecipientMatchesButAmountIsNegative() {

        // given
        val invoice = createInvoice()

        doReturn(createTransactionsIncluding(createTransaction((-1) * InvoiceTotalAmount, "noise", InvoiceRecipientName)))
            .`when`(transactionDaoMock).getAll()


        // when
        val result = underTest.findAccountTransactionThatMatchesDocument(invoice)


        // then
        assertThat(result).isNull()
    }


    private fun createInvoice(): Document {
        return Document.createInvoice(listOf(DocumentItem(0f, InvoiceTotalAmount)),
            InvoiceNumber, recipient = Company(InvoiceRecipientName, PersonType.Client, Address()))
    }

    private fun createTransactionsIncluding(transaction: BankAccountTransaction): List<BankAccountTransaction> {
        return listOf(
            transaction,
            createTransaction(1000.0, "noise", "noise"),
            createTransaction(2000.0, "noise", "noise"),
            createTransaction(3000.0, "noise", "noise"),
            createTransaction(4000.0, "noise", "noise"),
            createTransaction(5000.0, "noise", "noise"),
            createTransaction(6000.0, "noise", "noise"),
            createTransaction(7000.0, "noise", "noise"),
            createTransaction(8000.0, "noise", "noise"),
            createTransaction(9000.0, "noise", "noise")
        )
    }

    private fun createTransaction(amount: Double, usage: String, senderOrReceiver: String): BankAccountTransaction {
        return BankAccountTransaction(BigDecimal.valueOf(amount), usage, true, senderOrReceiver, "", "",
            Date(), "", "", BigDecimal.ZERO, BankAccount()
        )
    }
}