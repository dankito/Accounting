package net.dankito.accounting.javafx.windows.mainwindow

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.utils.datetime.DateConvertUtils
import java.text.NumberFormat
import java.time.LocalDate
import java.util.*


open class OverviewPresenter(private val documentService: IDocumentService,
                             private val router: Router,
                             private val vatCalculator: ValueAddedTaxCalculator = ValueAddedTaxCalculator()
) {


    companion object {
        val CurrencyFormat = NumberFormat.getCurrencyInstance()
    }


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

    fun calculateVatFromNetAmount(document: Document): Double {
        return vatCalculator.calculateVatFromNetAmount(document.netAmount, document.valueAddedTaxRate)
    }

    fun calculateVatFromTotalAmount(document: Document): Double {
        return vatCalculator.calculateVatFromTotalAmount(document.totalAmount, document.valueAddedTaxRate)
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


    fun getCurrencyString(amount: Double): String {
        return CurrencyFormat.format(amount)
    }

    fun getDefaultVatRateForUser(): Float {
        return 19f // TODO: make country / Locale specific
    }

    fun getVatRatesForUser(): List<Float> {
        return listOf(0f, 7f, 19f) // TODO: make country / Locale specific
    }


    fun getDocumentsInCurrentAndPreviousAccountingPeriod(documents: List<Document>): List<Document> {
        val periodStart = getPreviousAccountingPeriodStartDate()
        val periodEnd = getCurrentAccountingPeriodEndDate()

        return getDocumentsInPeriod(documents, periodStart, periodEnd)
    }

    fun getDocumentsInCurrentAccountingPeriod(documents: List<Document>): List<Document> {
        val periodStart = getCurrentAccountingPeriodStartDate()
        val periodEnd = getCurrentAccountingPeriodEndDate()

        return getDocumentsInPeriod(documents, periodStart, periodEnd)
    }

    fun getDocumentsInPreviousAccountingPeriod(documents: List<Document>): List<Document> {
        val periodStart = getPreviousAccountingPeriodStartDate()
        val periodEnd = getPreviousAccountingPeriodEndDate()

        return getDocumentsInPeriod(documents, periodStart, periodEnd)
    }

    private fun getDocumentsInPeriod(documents: List<Document>, periodStart: Date, periodEnd: Date): List<Document> {
        return documents.filter {
            it.paymentDate?.let { date -> date in periodStart..periodEnd }
            ?: false
        }
    }


    fun calculateCurrentAccountingPeriodRevenues(): Double {
        val currentPeriodRevenues = getDocumentsInCurrentAccountingPeriod(getRevenues())

        return sumTotalAmount(currentPeriodRevenues)
    }

    fun calculateCurrentAccountingPeriodNetRevenues(): Double {
        val currentPeriodRevenues = getDocumentsInCurrentAccountingPeriod(getRevenues())

        return sumNetAmount(currentPeriodRevenues)
    }

    fun calculateCurrentAccountingPeriodExpenditures(): Double {
        val currentPeriodExpenditures = getDocumentsInCurrentAccountingPeriod(getExpenditures())

        return sumTotalAmount(currentPeriodExpenditures)
    }

    fun calculateCurrentAccountingPeriodBalance(): Double {
        return calculateCurrentAccountingPeriodNetRevenues() - calculateCurrentAccountingPeriodExpenditures()
    }

    fun calculatePreviousAccountingPeriodRevenues(): Double {
        val previousPeriodRevenues = getDocumentsInPreviousAccountingPeriod(getRevenues())

        return sumTotalAmount(previousPeriodRevenues)
    }

    fun calculatePreviousAccountingPeriodNetRevenues(): Double {
        val previousPeriodRevenues = getDocumentsInPreviousAccountingPeriod(getRevenues())

        return sumNetAmount(previousPeriodRevenues)
    }

    fun calculatePreviousAccountingPeriodExpenditures(): Double {
        val previousPeriodExpenditures = getDocumentsInPreviousAccountingPeriod(getExpenditures())

        return sumTotalAmount(previousPeriodExpenditures)
    }

    fun calculatePreviousAccountingPeriodBalance(): Double {
        return calculatePreviousAccountingPeriodNetRevenues() - calculatePreviousAccountingPeriodExpenditures()
    }

    fun sumTotalAmount(previousPeriodExpenditures: List<Document>) =
        previousPeriodExpenditures.sumByDouble { it.totalAmount }

    fun sumNetAmount(previousPeriodExpenditures: List<Document>) =
        previousPeriodExpenditures.sumByDouble { it.netAmount }


    fun calculateCurrentAccountingPeriodReceivedVat(): Double {
        val currentPeriodRevenues = getDocumentsInCurrentAccountingPeriod(getRevenues())

        return sumVat(currentPeriodRevenues)
    }

    fun calculateCurrentAccountingPeriodSpentVat(): Double {
        val currentPeriodExpenditures = getDocumentsInCurrentAccountingPeriod(getExpenditures())

        return sumVat(currentPeriodExpenditures)
    }

    fun calculateCurrentAccountingPeriodVatBalance(): Double {
        return calculateCurrentAccountingPeriodReceivedVat() - calculateCurrentAccountingPeriodSpentVat()
    }

    fun calculatePreviousAccountingPeriodReceivedVat(): Double {
        val previousPeriodRevenues = getDocumentsInPreviousAccountingPeriod(getRevenues())

        return sumVat(previousPeriodRevenues)
    }

    fun calculatePreviousAccountingPeriodSpentVat(): Double {
        val previousPeriodExpenditures = getDocumentsInPreviousAccountingPeriod(getExpenditures())

        return sumVat(previousPeriodExpenditures)
    }

    fun calculatePreviousAccountingPeriodVatBalance(): Double {
        return calculatePreviousAccountingPeriodReceivedVat() - calculatePreviousAccountingPeriodSpentVat()
    }

    fun sumVat(previousPeriodExpenditures: List<Document>) =
        previousPeriodExpenditures.sumByDouble { it.valueAddedTax }


    fun getCurrentAccountingPeriodStartDate(): Date {
        val periodStart = getCurrentAccountingPeriodStartLocalDate()

        return DateConvertUtils.asUtilDate(periodStart)!!
    }

    fun getCurrentAccountingPeriodEndDate(): Date {
        val periodStart = getCurrentAccountingPeriodStartLocalDate()

        val periodEnd = if (accountingPeriod == AccountingPeriod.Monthly) {
            periodStart.plusMonths(1)
        } else {
            periodStart.plusMonths(3)
        }
            .minusDays(1)

        return DateConvertUtils.asUtilDate(periodEnd)!!
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

    // TODO: when adding support for Android find a solution without Java 8's LocalDate
    private fun getPreviousAccountingPeriodStartLocalDate(): LocalDate {
        val currentAccountingPeriodStartDate = getCurrentAccountingPeriodStartLocalDate()

        return if (accountingPeriod == AccountingPeriod.Monthly) {
            currentAccountingPeriodStartDate.minusMonths(1)
        } else {
            currentAccountingPeriodStartDate.minusMonths(3)
        }
    }

    private fun getCurrentAccountingPeriodStartLocalDate(): LocalDate {
        val today = getToday()

        var periodStart = today.withDayOfMonth(1)

        if (accountingPeriod == AccountingPeriod.Quarterly) {
            periodStart = when {
                periodStart.monthValue > 9 -> periodStart.withMonth(10)
                periodStart.monthValue > 6 -> periodStart.withMonth(7)
                periodStart.monthValue > 3 -> periodStart.withMonth(4)
                else -> periodStart.withMonth(1)
            }
        }

        return periodStart
    }

    // to be overrideable in unit tests
    protected open fun getToday(): LocalDate = LocalDate.now()


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