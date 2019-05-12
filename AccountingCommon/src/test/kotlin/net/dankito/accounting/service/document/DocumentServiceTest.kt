package net.dankito.accounting.service.document

import net.dankito.accounting.data.dao.DocumentDao
import net.dankito.accounting.data.dao.DocumentItemDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState
import net.dankito.accounting.service.util.db.DatabaseBasedTest
import net.dankito.utils.events.RxEventBus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class DocumentServiceTest : DatabaseBasedTest() {


    private val underTest: DocumentService = DocumentService(DocumentDao(entityManager), DocumentItemDao(entityManager), RxEventBus())


    @Test
    fun saveRevenue() {

        // given
        val revenue = Document(DocumentType.Revenue, 33.5, 19f)

        assertThat(underTest.getRevenues()).isEmpty()
        assertThat(underTest.getExpenditures()).isEmpty()
        assertThat(underTest.getCreatedInvoices()).isEmpty()


        // when
        underTest.saveOrUpdate(revenue)


        // then
        assertThat(underTest.getExpenditures()).isEmpty()
        assertThat(underTest.getCreatedInvoices()).isEmpty()

        assertThat(underTest.getRevenues()).hasSize(1)
        assertThat(underTest.getRevenues().get(0)).isEqualTo(revenue)
    }

    @Test
    fun saveExpenditure() {

        // given
        val expenditure = Document(DocumentType.Expenditure, 33.5, 19f)

        assertThat(underTest.getRevenues()).isEmpty()
        assertThat(underTest.getExpenditures()).isEmpty()
        assertThat(underTest.getCreatedInvoices()).isEmpty()


        // when
        underTest.saveOrUpdate(expenditure)


        // then
        assertThat(underTest.getRevenues()).isEmpty()
        assertThat(underTest.getCreatedInvoices()).isEmpty()

        assertThat(underTest.getExpenditures()).hasSize(1)
        assertThat(underTest.getExpenditures().get(0)).isEqualTo(expenditure)
    }

    @Test
    fun saveCreatedInvoice() {

        // given
        val invoice = createInvoice()

        assertThat(underTest.getRevenues()).isEmpty()
        assertThat(underTest.getExpenditures()).isEmpty()
        assertThat(underTest.getCreatedInvoices()).isEmpty()


        // when
        underTest.saveOrUpdate(invoice)


        // then
        assertThat(underTest.getRevenues()).isEmpty()
        assertThat(underTest.getExpenditures()).isEmpty()

        assertThat(underTest.getCreatedInvoices()).hasSize(1)
        assertThat(underTest.getCreatedInvoices().get(0)).isEqualTo(invoice)

        assertThat(invoice.items).isNotEmpty
        invoice.items.forEach { documentItem ->
            assertThat(documentItem.isPersisted()).isTrue()
        }
    }

    @Test
    fun invoiceGetsPaid_IsNowARevenue() {

        // given
        val invoice = createInvoice()

        underTest.saveOrUpdate(invoice)

        assertThat(underTest.getRevenues()).isEmpty()
        assertThat(underTest.getExpenditures()).isEmpty()

        assertThat(underTest.getCreatedInvoices()).hasSize(1)
        assertThat(underTest.getCreatedInvoices().get(0)).isEqualTo(invoice)


        // when
        invoice.paymentState = PaymentState.Paid


        // then
        assertThat(underTest.getExpenditures()).isEmpty()

        assertThat(underTest.getRevenues()).hasSize(1)
        assertThat(underTest.getRevenues().get(0)).isEqualTo(invoice)

        assertThat(underTest.getCreatedInvoices()).hasSize(1)
        assertThat(underTest.getCreatedInvoices().get(0)).isEqualTo(invoice)

        assertThat(invoice.items).isNotEmpty
        invoice.items.forEach { documentItem ->
            assertThat(documentItem.isPersisted()).isTrue()
        }
    }

    private fun createInvoice(): Document {
        val items = listOf(DocumentItem(1000.0, 19f, 190.0, 1190.0, "Webshop for Amazon"))

        return Document.createInvoice(items, "1")
    }

}