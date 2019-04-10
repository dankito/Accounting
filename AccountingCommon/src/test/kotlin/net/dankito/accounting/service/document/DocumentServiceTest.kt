package net.dankito.accounting.service.document

import net.dankito.accounting.data.dao.JsonDocumentDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState
import net.dankito.utils.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import java.io.File

class DocumentServiceTest {

    private val dataFolder = File("testData")

    private val fileUtils = FileUtils()

    private val underTest: DocumentService


    init {
        clearDataFolder()

        underTest = DocumentService(JsonDocumentDao(dataFolder))
    }

    @After
    fun tearDown() {
        clearDataFolder()
    }


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
        val invoice = Document.createInvoice(1000.0, 19f, 190.0, 1190.0,
            "1", "Webshop for Amazon")

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
    }

    @Test
    fun invoiceGetsPaid_IsNowARevenue() {

        // given
        val invoice = Document.createInvoice(1000.0, 19f, 190.0, 1190.0,
            "1", "Webshop for Amazon")

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
    }


    private fun clearDataFolder() {
        fileUtils.deleteFolderRecursively(dataFolder)
    }

}