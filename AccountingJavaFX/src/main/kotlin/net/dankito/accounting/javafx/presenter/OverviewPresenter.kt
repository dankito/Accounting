package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.DocumentType
import net.dankito.accounting.data.model.PaymentState
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.event.AccountingPeriodChangedEvent
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.filter.EntityFilter
import net.dankito.accounting.data.model.settings.AppSettings
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.filter.IFilterService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.events.IEventBus
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.concurrent.schedule


open class OverviewPresenter(private val documentService: IDocumentService,
                             private val settingsService: ISettingsService,
                             private val bankAccountService: IBankAccountService,
                             private val filterService: IFilterService,
                             private val eventBus: IEventBus,
                             private val router: Router,
                             private val vatCalculator: ValueAddedTaxCalculator = ValueAddedTaxCalculator()
) {


    companion object {
        val CurrencyFormat = NumberFormat.getCurrencyInstance()

        val ShortDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
        val MediumDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

        private val log = LoggerFactory.getLogger(OverviewPresenter::class.java)
    }


    init {
        Timer().schedule(1 * 1000) {
            checkUnpaidInvoicesPaymentState()
        }

        eventBus.subscribe(BankAccountTransactionsUpdatedEvent::class.java) { event ->
            runAccountTransactionsFilterAndCreateNewDocuments(event.updatedTransactions)
        }
    }


    var accountingPeriod: AccountingPeriod = settingsService.appSettings.accountingPeriod
        set(newAccountingPeriod) {
            if (newAccountingPeriod != field) {
                field = newAccountingPeriod

                accountingPeriodChanged(newAccountingPeriod)
            }
        }

    val settings: AppSettings = settingsService.appSettings


    val isBankAccountAdded: Boolean
        get() {
            return bankAccountService.getBankAccounts().isNotEmpty()
        }


    private fun accountingPeriodChanged(newAccountingPeriod: AccountingPeriod) {
        eventBus.post(AccountingPeriodChangedEvent())

        settingsService.appSettings.accountingPeriod = newAccountingPeriod
        saveAppSettings()
    }

    fun saveAppSettings() {
        settingsService.saveAppSettings()
    }


    fun saveOrUpdate(document: Document) {
        updateVat(document)

        documentService.saveOrUpdate(document)

        document.createdFromAccountTransaction?.let {
            bankAccountService.saveOrUpdateTransaction(it)
        }

        if (document.isSelfCreatedInvoice && document.paymentState != PaymentState.Paid) {
            checkInvoicePaymentState(document)
        }
    }

    private fun updateVat(document: Document) {
        if (document.isValueAddedTaxRateSet) {
            if (document.isTotalAmountSet) {
                document.valueAddedTax = calculateVatFromTotalAmount(document)

                document.netAmount = document.totalAmount - document.valueAddedTax
            }
            else if (document.isNetAmountSet) {
                document.valueAddedTax = calculateVatFromNetAmount(document)

                document.totalAmount = document.netAmount + document.valueAddedTax
            }
        }
    }


    fun delete(documents: List<Document>) {
        documents.forEach { delete(it) }
    }

    fun delete(document: Document) {
        document.createdFromAccountTransaction?.let { transaction ->
            transaction.createdDocument = null

            bankAccountService.saveOrUpdateTransaction(transaction)
        }

        documentService.delete(document)
    }


    fun checkUnpaidInvoicesPaymentState() {
        bankAccountService.updateAccountsTransactionsAsync { transactions ->
            documentService.getUnpaidCreatedInvoices().forEach { unpaidInvoice ->
                checkInvoicePaymentState(unpaidInvoice)
            }
        }
    }

    fun checkInvoicePaymentState(invoice: Document) {
        if (invoice.paymentState != PaymentState.Paid) {
            bankAccountService.findAccountTransactionThatMatchesDocument(invoice)?.let { transaction ->
                invoice.paymentState = PaymentState.Paid
                invoice.paymentDate = transaction.valueDate

                invoice.createdFromAccountTransaction = transaction

                transaction.createdDocument = invoice

                saveOrUpdate(invoice)
            }
        }
    }


    fun saveOrUpdate(entityFilter: EntityFilter) {
        filterService.saveOrUpdate(entityFilter)
    }

    fun runAccountTransactionsFilterAndCreateNewDocuments(transactions: Collection<BankAccountTransaction>) {
        runFilterAndCreateNewDocuments(getAccountTransactionsEntityFilters(), transactions)
    }

    fun <T> runFilterAndCreateNewDocuments(entityFilters: List<EntityFilter>, collectionToFilter: Collection<T>) {
        entityFilters.forEach { entityFilter ->
            val itemsOnWithFilterApplies = filterService.filterCollection(entityFilter, collectionToFilter)

            // TODO: create ICreateDocumentFromEntity interface and a factory to get actual implementation, e. g. for BankAccountTransation, Email, ...
            (itemsOnWithFilterApplies as? Collection<BankAccountTransaction>)?.let {
                addToExpendituresAndRevenues(itemsOnWithFilterApplies, entityFilter)
            }
        }
    }

    fun getAccountTransactionsEntityFilters(): List<EntityFilter> {
        return getEntityFiltersForEntity(BankAccountTransaction::class.java.name)
    }

    private fun getEntityFiltersForEntity(entityClass: String): List<EntityFilter> {
        return filterService.getEntityFiltersForEntity(entityClass)
    }



    fun addToExpendituresAndRevenues(transactions: Collection<BankAccountTransaction>,
                                     automaticallyCreatedFromFilter: EntityFilter? = null) {

        val vatRate = getDefaultVatRateForUser()

        val documents = createDocumentsForTransactions(transactions, vatRate, automaticallyCreatedFromFilter)

        documents.forEach {
            saveOrUpdate(it)
        }
    }

    fun adjustBeforeAddingToExpendituresAndRevenues(transactions: Collection<BankAccountTransaction>) {
        val vatRate = getDefaultVatRateForUser()

        val documents = createDocumentsForTransactions(transactions, vatRate)

        documents.forEach {
            showEditDocumentWindow(it)
        }
    }

    private fun createDocumentsForTransactions(transactions: Collection<BankAccountTransaction>, valueAddedTaxRate: Float,
                                               automaticallyCreatedFromFilter: EntityFilter? = null): List<Document> {

        // don't create a second document from the same account transaction
        val transactionsWithNotYetCreatedDocuments = transactions.filter { it.createdDocument == null }

        return transactionsWithNotYetCreatedDocuments.map { transaction ->
            mapTransactionToDocument(transaction, valueAddedTaxRate, automaticallyCreatedFromFilter)
        }
    }

    private fun mapTransactionToDocument(transaction: BankAccountTransaction, valueAddedTaxRate: Float,
        automaticallyCreatedFromFilter: EntityFilter? = null): Document {

        val type = if (transaction.isDebit) DocumentType.Expenditure else DocumentType.Revenue

        val document = Document(type, Math.abs(transaction.amount.toDouble()), valueAddedTaxRate)

        document.paymentDate = transaction.valueDate
        document.description = transaction.senderOrReceiverName + " - " + transaction.usage

        transaction.createdDocument = document
        document.createdFromAccountTransaction = transaction
        document.automaticallyCreatedFromFilter = automaticallyCreatedFromFilter

        return document
    }


    /**
     * [roundDownNetAmount] is needed for revenues in Germany where you first have to round down the net amount and
     * calculate VAT from this value.
     */
    @JvmOverloads
    fun calculateVatFromNetAmount(document: Document, roundDownNetAmount: Boolean = false): Double {
        return vatCalculator.calculateVatFromNetAmount(document.netAmount, document.valueAddedTaxRate, roundDownNetAmount)
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


    fun getCurrencyString(amount: BigDecimal): String {
        return getCurrencyString(amount.toDouble())
    }

    fun getCurrencyString(amount: Double): String {
        return CurrencyFormat.format(amount)
    }

    fun getUserCurrencySymbol(): String {
        return (CurrencyFormat as DecimalFormat).positiveSuffix
    }

    fun getDefaultVatRateForUser(): Float {
        return 19f // TODO: make country / Locale specific
    }

    fun getVatRatesForUser(): List<Float> {
        return listOf(0f, 7f, 19f) // TODO: make country / Locale specific
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

    fun getDocumentsInPeriod(documents: List<Document>, periodStart: Date, periodEnd: Date): List<Document> {
        return documents.filter {
            isDocumentInPeriod(it, periodStart, periodEnd)
        }
    }

    fun isInCurrentAccountingPeriod(document: Document): Boolean {
        return isDocumentInPeriod(document, getCurrentAccountingPeriodStartDate(), getCurrentAccountingPeriodEndDate())
    }

    fun isInPreviousAccountingPeriod(document: Document): Boolean {
        return isDocumentInPeriod(document, getPreviousAccountingPeriodStartDate(), getPreviousAccountingPeriodEndDate())
    }

    private fun isDocumentInPeriod(document: Document, periodStart: Date, periodEnd: Date): Boolean {
        return isDateInPeriod(document.paymentDate, periodStart, periodEnd)
    }

    private fun isDateInPeriod(date: Date?, periodStart: Date, periodEnd: Date): Boolean {
        return date?.let { date in periodStart..periodEnd }
            ?: false
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

        return periodStart.asUtilDate()
    }

    fun getCurrentAccountingPeriodEndDate(): Date {
        val periodStart = getCurrentAccountingPeriodStartLocalDate()

        return getAccountingPeriodEndDate(periodStart)
    }

    fun getPreviousAccountingPeriodStartDate(): Date {
        val periodStart = getPreviousAccountingPeriodStartLocalDate()

        return periodStart.asUtilDate()
    }

    fun getPreviousAccountingPeriodEndDate(): Date {
        val periodStart = getPreviousAccountingPeriodStartLocalDate()

        return getAccountingPeriodEndDate(periodStart)
    }

    @JvmOverloads
    fun getAccountingPeriodEndDate(periodStart: LocalDate, accountingPeriod: AccountingPeriod = this.accountingPeriod): Date {
        val nextPeriodStart = when (accountingPeriod) {
            AccountingPeriod.Monthly -> periodStart.plusMonths(1)
            AccountingPeriod.Quarterly -> periodStart.plusMonths(3)
            AccountingPeriod.Annually -> periodStart.plusMonths(12)
        }

        val periodEnd = nextPeriodStart.minusDays(1)

        return periodEnd.asUtilDate()
    }

    // TODO: when adding support for Android find a solution without Java 8's LocalDate
    private fun getPreviousAccountingPeriodStartLocalDate(): LocalDate {
        val currentAccountingPeriodStartDate = getCurrentAccountingPeriodStartLocalDate()

        return when (accountingPeriod) {
            AccountingPeriod.Monthly -> currentAccountingPeriodStartDate.minusMonths(1)
            AccountingPeriod.Quarterly -> currentAccountingPeriodStartDate.minusMonths(3)
            AccountingPeriod.Annually -> currentAccountingPeriodStartDate.minusMonths(12)
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
        else if (accountingPeriod == AccountingPeriod.Annually) {
            periodStart = periodStart.withDayOfYear(1)
        }

        return periodStart
    }

    // to be overrideable in unit tests
    protected open fun getToday(): LocalDate = LocalDate.now()


    fun filterDocuments(allDocuments: List<Document>, filterTerm: String): List<Document> {
        if (filterTerm.isEmpty()) {
            return allDocuments
        }
        else {
            return allDocuments.filter { doesDocumentsFilterApply(it, filterTerm) }
        }
    }

    fun doesDocumentsFilterApply(document: Document, filterTerm: String): Boolean {
        val lowerCaseFilter = filterTerm.toLowerCase()

        return document.description?.toLowerCase()?.contains(lowerCaseFilter) == true
                || doesDateFilterApply(document.paymentDate, lowerCaseFilter)
                || doesCurrencyFilterApply(document.netAmount, lowerCaseFilter)
                || doesCurrencyFilterApply(document.totalAmount, lowerCaseFilter)
    }

    private fun doesDateFilterApply(date: Date?, lowerCaseFilter: String): Boolean {
        if (date != null) {
            try {
                return ShortDateFormat.format(date).toLowerCase().contains(lowerCaseFilter)
                        || MediumDateFormat.format(date).toLowerCase().contains(lowerCaseFilter)
            } catch (e: Exception) {
                log.warn("Could not test if filter '$lowerCaseFilter' applies to date '$date'", e)
            }
        }

        return false
    }

    private fun doesCurrencyFilterApply(amount: Double, lowerCaseFilter: String): Boolean {
        try {
            val amountString = String.format("%.2f", amount)

            return amountString.contains(lowerCaseFilter)
        } catch (e: Exception) {
            log.warn("Could not test if amount '$amount' contains filter '$lowerCaseFilter'", e)
        }

        return false
    }


    fun showCreateInvoiceWindow() {
        router.showCreateInvoiceWindow()
    }

    fun showCreateRevenueWindow() {
        val newRevenue = Document(DocumentType.Revenue)

        router.showEditDocumentWindow(newRevenue, this)
    }

    fun showCreateExpenditureWindow() {
        val newExpenditure = Document(DocumentType.Expenditure)

        router.showEditDocumentWindow(newExpenditure, this)
    }

    fun showEditDocumentWindow(document: Document) {
        router.showEditDocumentWindow(document, this)
    }

}