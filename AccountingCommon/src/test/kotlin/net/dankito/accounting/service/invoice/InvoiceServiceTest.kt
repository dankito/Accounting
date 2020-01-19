package net.dankito.accounting.service.invoice

import net.dankito.accounting.data.dao.invoice.ICreateInvoiceSettingsDao
import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.data.model.invoice.CreateInvoiceJob
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.Person
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.accounting.service.person.IPersonService
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito.mock
import java.awt.Desktop
import java.io.File
import java.text.SimpleDateFormat


class InvoiceServiceTest {

    companion object {
        private val DocumentNumberFormat = SimpleDateFormat("yyMMdd")

        private const val TemplateFileName = "InvoiceTemplate.odt"

        private val templateFileUrl = InvoiceServiceTest::class.java.classLoader.getResource(TemplateFileName)
        private val TemplateFile = File(templateFileUrl.toURI())
    }


    private val createInvoiceSettingsDaoMock = mock(ICreateInvoiceSettingsDao::class.java)

    private val personServiceMock = mock(IPersonService::class.java)

    private val underTest = InvoiceService(createInvoiceSettingsDaoMock, personServiceMock)


    @Ignore // not an automatic test, creates and displays a PDF file
    @Test
    fun createInvoice() {
        val documentNumber = "190327"
        val issueDate = DocumentNumberFormat.parse(documentNumber)

        val outputFile = File.createTempFile("InvoiceServiceTest_CreatedInvoice", ".pdf")

        val invoiceItems = listOf(
            createDocumentItem(0, "Support back end development", 75.0, 113.92),
            createDocumentItem(1, "Consulting", 95.0, 20.4)
        )

        underTest.createInvoice(CreateInvoiceJob(
            Document.createInvoice(invoiceItems, documentNumber, null, issueDate, null, createRecipient()),
            TemplateFile, outputFile
        ))

        showFile(outputFile)
    }


    private fun createDocumentItem(index: Int, description: String, unitPrice: Double, quantity: Double): DocumentItem {
        val netAmount = unitPrice * quantity
        val vatRate = 0.19f
        val vat = netAmount * vatRate
        val totalAmount = netAmount + vat

        return DocumentItem(index, description, unitPrice, quantity, netAmount, vatRate, vat, totalAmount)
    }

    private fun createRecipient(): NaturalOrLegalPerson? {
        return Person("Marieke", "Musterfrau", PersonType.Client, Address("Musterstraße", "42", "12345", "Musterstedt", "Germany"))
    }


    private fun showFile(file: File) {
        Desktop.getDesktop().open(file)
    }

}