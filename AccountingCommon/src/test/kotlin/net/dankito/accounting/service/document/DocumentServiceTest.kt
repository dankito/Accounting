package net.dankito.accounting.service.document

import net.dankito.accounting.data.dao.DocumentDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState
import net.dankito.accounting.service.util.db.JavaCouchbaseLiteEntityManager
import net.dankito.jpa.couchbaselite.CouchbaseLiteEntityManagerBase
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.utils.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import java.io.File


class DocumentServiceTest {

    private val fileUtils = FileUtils()


    private val dataFolder = File("testData")

    private val entityManagerConfiguration = EntityManagerConfiguration(dataFolder.path, "accounting")

    private val entityManager: CouchbaseLiteEntityManagerBase


    private val underTest: DocumentService


    init {
        clearDataFolder()

        entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)

        underTest = DocumentService(DocumentDao(entityManager))
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
    }

    private fun createInvoice(): Document {
        val items = listOf(DocumentItem(1000.0, 19f, 190.0, 1190.0, "Webshop for Amazon"))

        return Document.createInvoice(items, "1")
    }


    private fun clearDataFolder() {
        fileUtils.deleteFolderRecursively(dataFolder)
    }

}