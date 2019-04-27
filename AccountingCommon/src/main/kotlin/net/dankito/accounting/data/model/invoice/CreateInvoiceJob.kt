package net.dankito.accounting.data.model.invoice

import net.dankito.accounting.data.model.Document
import java.io.File

open class CreateInvoiceJob(val invoice: Document,
                            val templateFile: File,
                            val pdfOutputFile: File)