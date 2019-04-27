package net.dankito.accounting.service.invoice

import net.dankito.accounting.data.model.invoice.CreateInvoiceJob
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import java.io.File
import java.util.*


interface IInvoiceService {

    val settings: CreateInvoiceSettings

    fun saveSettings()


    fun createInvoice(job: CreateInvoiceJob)

    fun calculateInvoiceNumber(invoicingDate: Date): String

    fun createDefaultPdfOutputFile(job: CreateInvoiceJob): File

}