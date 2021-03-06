package net.dankito.accounting.service.invoice

import fr.opensagres.xdocreport.converter.ConverterTypeTo
import fr.opensagres.xdocreport.converter.Options
import fr.opensagres.xdocreport.core.document.DocumentKind
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry
import fr.opensagres.xdocreport.template.TemplateEngineKind
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata
import net.dankito.accounting.data.dao.invoice.ICreateInvoiceSettingsDao
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentItem
import net.dankito.accounting.data.model.invoice.CreateInvoiceJob
import net.dankito.accounting.data.model.invoice.CreateInvoiceSettings
import net.dankito.accounting.data.model.invoice.FormattedInvoice
import net.dankito.accounting.data.model.invoice.FormattedInvoiceItem
import net.dankito.accounting.service.person.IPersonService
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*


open class InvoiceService(protected val dao: ICreateInvoiceSettingsDao, protected val personService: IPersonService)
    : IInvoiceService {


    companion object {
        val InvoiceFilenameDateformat = SimpleDateFormat("yyyy.MM.dd")

        val InvoicingDateFormat = SimpleDateFormat("dd.MM.yyyy")

        val InvoiceStartDateFormat = SimpleDateFormat("dd. MMMM")

        val InvoiceEndDateFormat = SimpleDateFormat("dd. MMMM yyyy")

        val InvoiceNumberFromInvoicingDateFormat = SimpleDateFormat("yyMMdd'1'")


        private val log = LoggerFactory.getLogger(InvoiceService::class.java)

    }


    override val settings: CreateInvoiceSettings = retrieveOrCreateSettings()


    override fun saveSettings() {
        personService.saveOrUpdate(settings.lastSelectedRecipient)

        dao.saveOrUpdate(settings)
    }


    protected open fun retrieveOrCreateSettings(): CreateInvoiceSettings {
        val all = dao.getAll()

        if (all.isEmpty()) {
            val newSettings = CreateInvoiceSettings()

            personService.saveOrUpdate(newSettings.lastSelectedRecipient)
            dao.saveOrUpdate(newSettings)

            return newSettings
        }
        else {
            if (all.size > 1) { // should never be the case that all contains more than one CreateInvoiceSettings instance
                log.warn("There are more than on CreateInvoiceSettings persisted. Returning first one: ${all[0]}")
            }

            return all[0]
        }
    }


    override fun createInvoice(job: CreateInvoiceJob) {
        val report = XDocReportRegistry.getRegistry().loadReport(FileInputStream(job.templateFile),
            TemplateEngineKind.Velocity)

        val context = report.createContext()

        val invoice = mapToInvoice(job.invoice)
        context.put("invoice", invoice)

        // this is XDocReporter's logic to show items. The template here is a row in a table
        val metadata = FieldsMetadata()
        metadata.addFieldAsList("invoiceItem.index")
        metadata.addFieldAsList("invoiceItem.description")
        metadata.addFieldAsList("invoiceItem.unitPrice")
        metadata.addFieldAsList("invoiceItem.quantity")
        metadata.addFieldAsList("invoiceItem.netAmount")
        report.fieldsMetadata = metadata
        context.put("invoiceItem", invoice.items)

        job.pdfOutputFile.parentFile?.mkdirs()

        val pdfOutputStream = FileOutputStream(job.pdfOutputFile)

        val options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF)

        report.convert(context, options, pdfOutputStream)

        pdfOutputStream.close()
    }

    protected open fun mapToInvoice(document: Document): FormattedInvoice {
        val invoicingDate = document.issueDate ?: Date()

        return FormattedInvoice(document.items.map { mapToInvoiceItem(it) },
            InvoicingDateFormat.format(document.issueDate),
            document.documentNumber ?: "",
            formatCurrency(document.netAmount),
            formatPercentageString(document.valueAddedTaxRates.first()), // TODO: add a line for each different value added tax rate
            formatCurrency(document.valueAddedTax),
            formatCurrency(document.totalAmount),
            formatDate(calculateInvoiceStartDate(invoicingDate), InvoiceStartDateFormat),
            formatDate(calculateInvoiceEndDate(invoicingDate), InvoiceEndDateFormat),
            calculateTimeForPayment(document),
            document.recipient
        )
    }

    protected open fun mapToInvoiceItem(documentItem: DocumentItem): FormattedInvoiceItem {
        return FormattedInvoiceItem(
            documentItem.index + 1, // one based
            documentItem.description ?: "",
            formatCurrency(documentItem.unitPrice),
            formatQuantityString(documentItem.quantity),
            formatCurrency(documentItem.netAmount)
        )
    }


    protected open fun formatCurrency(value: Double, locale: Locale = Locale.getDefault()): String {
        return NumberFormat.getCurrencyInstance(locale).format(value)
    }

    protected open fun formatQuantityString(quantity: Double, countDecimalPlaces: Int = 2): String {
        return String.format("%.${countDecimalPlaces}f", quantity)
    }

    protected open fun formatPercentageString(percentage: Float, countDecimalPlaces: Int = 0): String {
        return String.format("%.${countDecimalPlaces}f", percentage) + " %"
    }

    protected open fun formatDate(date: Date, dateFormat: DateFormat): String {
        return dateFormat.format(date)
    }

    protected open fun round(value: Double, countDecimalPlaces: Int): Double {
        var decimal = BigDecimal(value)

        decimal = decimal.setScale(countDecimalPlaces, RoundingMode.HALF_UP)

        return decimal.toDouble()
    }

    // TODO: no magic here, don't calculate anything
    open fun calculateInvoiceStartDate(invoicingDate: Date): Date {
        return calculateInvoiceStartDate(invoicingDate.asLocalDate()).asUtilDate()
    }

    open fun calculateInvoiceStartDate(invoicingDate: LocalDate): LocalDate {
        if (isAtEndOfMonth(invoicingDate)) {
            return invoicingDate.withDayOfMonth(1)
        }
        else {
            return invoicingDate.minusMonths(1).withDayOfMonth(1)
        }
    }

    open fun calculateInvoiceEndDate(invoicingDate: Date): Date {
        return calculateInvoiceEndDate(invoicingDate.asLocalDate()).asUtilDate()
    }

    open fun calculateInvoiceEndDate(invoicingDate: LocalDate): LocalDate {
        if (isAtEndOfMonth(invoicingDate)) {
            return invoicingDate
        }
        else {
            val previousMonth = invoicingDate.minusMonths(1)

            return previousMonth.withDayOfMonth(previousMonth.lengthOfMonth())
        }
    }

    protected open fun isAtEndOfMonth(date: LocalDate): Boolean {
        return date.dayOfMonth >= date.lengthOfMonth() - 4
    }


    override fun calculateInvoiceNumber(invoicingDate: Date): String {
        return InvoiceNumberFromInvoicingDateFormat.format(invoicingDate)
    }

    protected open fun calculateTimeForPayment(document: Document): Int {
        document.dueDate?.let { dueDate ->
            document.issueDate?.let { issueDate ->
                val timeForPaymentInMillis = dueDate.time - issueDate.time

                return (timeForPaymentInMillis / 1000 / 60 / 60 / 24).toInt()
            }
        }

        return 0
    }


    override fun createDefaultPdfOutputFile(job: CreateInvoiceJob): File {
        return File(job.templateFile.parentFile, "%04d.%02d.%02d ${job.invoice.recipient?.name}.pdf"
                .format(InvoiceFilenameDateformat.format(job.invoice.issueDate)))
    }

}