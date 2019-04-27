package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.data.model.invoice.CreateInvoiceJob
import net.dankito.accounting.service.invoice.IInvoiceService
import net.dankito.utils.javafx.os.JavaFxOsService
import java.io.File


class CreateInvoicePresenter(private val invoiceService: IInvoiceService, private val osService: JavaFxOsService) {

    val settings = invoiceService.settings

    fun saveSettings() {
        invoiceService.saveSettings()
    }


    fun createAndShowInvoice(invoice: Document) {
        val invoiceOutputFile = File(settings.invoiceOutputFilePath)

        createInvoiceFile(invoice, invoiceOutputFile)

        invoice.filePath = invoiceOutputFile.absolutePath

        openFileInOsDefaultApplication(invoiceOutputFile)
    }

    fun createInvoiceFile(invoice: Document, invoiceOutputFile: File) {
        val invoiceTemplateFile = File(settings.invoiceTemplateFilePath)

        invoiceService.createInvoice(CreateInvoiceJob(invoice, invoiceTemplateFile, invoiceOutputFile))
    }


    fun createDocumentItem(index: Int, quantity: Double): DocumentItem {
        return createDocumentItem(index, settings.invoiceItemDescription, settings.invoiceItemUnitPrice, quantity)
    }

    fun createDocumentItem(index: Int, description: String, unitPrice: Double, quantity: Double): DocumentItem {
        val netAmount = unitPrice * quantity
        val vatRate = 0.19f
        val vat = netAmount * vatRate
        val totalAmount = netAmount + vat

        return DocumentItem(index, description, unitPrice, quantity, netAmount, vatRate, vat, totalAmount)
    }


    private fun openFileInOsDefaultApplication(file: File) {
        osService.openFileInOsDefaultApplication(file)
    }

}