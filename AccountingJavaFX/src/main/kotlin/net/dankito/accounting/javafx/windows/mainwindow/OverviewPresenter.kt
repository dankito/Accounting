package net.dankito.accounting.javafx.windows.mainwindow

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.document.DocumentService
import net.dankito.utils.datetime.DateConvertUtils
import java.time.LocalDate
import java.util.*


class OverviewPresenter(private val documentService: DocumentService, private val router: Router) {


    var accountingPeriod: AccountingPeriod = AccountingPeriod.Monthly
        set(newAccountingPeriod) {
            if (newAccountingPeriod != field) {
                field = newAccountingPeriod

                callDocumentsUpdatedListeners()
            }
        }

    private val documentsUpdatedListeners = mutableListOf<() -> Unit>() // TODO: find a better event bus


    fun saveOrUpdate(document: Document) {
        setVatIfNotSet(document)

        documentService.saveOrUpdate(document)

        callDocumentsUpdatedListeners()
    }

    private fun setVatIfNotSet(document: Document) {
        if (document.isValueAddedTaxRateSet) {
            if (document.isTotalAmountSet) {
                if (document.isNetAmountSet == false) {
                    if (document.isValueAddedTaxSet == false) {
                        document.valueAddedTax = calculateVatFromTotalAmount(document)
                    }

                    document.netAmount = document.totalAmount - document.valueAddedTax
                }
            } else {
                if (document.isNetAmountSet) {
                    if (document.isValueAddedTaxSet == false) {
                        document.valueAddedTax = calculateVatFromNetAmount(document)
                    }

                    document.totalAmount = document.netAmount + document.valueAddedTax
                }
            }
        }
    }

    private fun calculateVatFromNetAmount(document: Document): Double {
        return document.valueAddedTaxRate * (1 + (document.totalAmount / 100f)) // TODO
    }

    private fun calculateVatFromTotalAmount(document: Document): Double {
        return document.valueAddedTaxRate * (document.totalAmount / 100f) // TODO
    }


    fun getRevenues(): List<Document> {
        return documentService.getRevenues()
    }

    fun getExpenditures(): List<Document> {
        return documentService.getExpenditures()
    }

    fun getCreatedInvoices(): List<Document> {
        return documentService.getCreatedInvoices()
    }


    fun getDefaultVatRateForUser(): Float {
        return 19f // TODO: make country / Locale specific
    }

    fun getVatRatesForUser(): List<Float> {
        return listOf(0f, 7f, 19f) // TODO: make country / Locale specific
    }


    fun getDocumentsInCurrentAndPreviousAccountingPeriod(documents: List<Document>): List<Document> {
        val previousAccountingPeriodStart = getPreviousAccountingPeriodStartDate()

        return documents.filter { it.paymentDate?.let { date -> date >= previousAccountingPeriodStart } ?: false }
    }

    fun getCurrentAccountingPeriodStartDate(): Date {
        val periodStart = getCurrentAccountingPeriodStartLocalDate()

        return DateConvertUtils.asUtilDate(periodStart)!!
    }

    fun getPreviousAccountingPeriodStartDate(): Date {
        val periodStart = getPreviousAccountingPeriodStartLocalDate()

        return DateConvertUtils.asUtilDate(periodStart)!!
    }

    fun getPreviousAccountingPeriodEndDate(): Date {
        val periodStart = getPreviousAccountingPeriodStartLocalDate()

        val periodEnd = if (accountingPeriod == AccountingPeriod.Monthly) {
            periodStart.plusMonths(1)
        } else {
            periodStart.plusMonths(3)
        }
            .minusDays(1)

        return DateConvertUtils.asUtilDate(periodEnd)!!
    }

    private fun getPreviousAccountingPeriodStartLocalDate(): LocalDate {
        val currentAccountingPeriodStartDate = getCurrentAccountingPeriodStartLocalDate()

        return if (accountingPeriod == AccountingPeriod.Monthly) {
            currentAccountingPeriodStartDate.minusMonths(1)
        } else {
            currentAccountingPeriodStartDate.minusMonths(3)
        }
    }

    private fun getCurrentAccountingPeriodStartLocalDate(): LocalDate {
        val today = LocalDate.now()

        var periodStart = today.withDayOfMonth(1)

        if (accountingPeriod == AccountingPeriod.Quarterly) {
            periodStart = when {
                periodStart.monthValue > 9 -> periodStart.withMonth(10)
                periodStart.monthValue > 9 -> periodStart.withMonth(7)
                periodStart.monthValue > 9 -> periodStart.withMonth(4)
                else -> periodStart.withMonth(1)
            }
        }

        return periodStart
    }


    fun showCreateRevenueWindow() {
        val newRevenue = Document(DocumentType.Revenue)

        router.showEditDocumentWindow(newRevenue, this)
    }

    fun showCreateExpenditureWindow() {
        val newExpenditure = Document(DocumentType.Expenditure)

        router.showEditDocumentWindow(newExpenditure, this)
    }

    fun showEditDocumentWindow(expenditure: Document) {
        router.showEditDocumentWindow(expenditure, this)
    }


    fun addDocumentsUpdatedListenerInAMemoryLeakWay(documentsUpdated: () -> Unit) {
        documentsUpdatedListeners.add(documentsUpdated)
    }

    private fun callDocumentsUpdatedListeners() {
        ArrayList(documentsUpdatedListeners).forEach { it() }
    }

}