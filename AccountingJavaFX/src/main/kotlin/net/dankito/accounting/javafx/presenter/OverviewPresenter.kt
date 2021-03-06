package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.*
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.event.AccountingPeriodChangedEvent
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.filter.EntityFilter
import net.dankito.accounting.data.model.filter.Filter
import net.dankito.accounting.data.model.invoice.InvoiceData
import net.dankito.accounting.data.model.settings.AppSettings
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.filter.IFilterService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.text.extraction.ITextExtractorRegistry
import net.dankito.text.extraction.info.invoice.InvoiceDataExtractor
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.events.IEventBus
import org.slf4j.LoggerFactory
import java.io.File
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
                             private val textExtractorRegistry: ITextExtractorRegistry,
                             private val invoiceDataExtractor: InvoiceDataExtractor,
                             private val eventBus: IEventBus,
                             private val router: Router,
                             private val vatCalculator: ValueAddedTaxCalculator = ValueAddedTaxCalculator()
) {


    companion object {
        val CurrencyFormat = NumberFormat.getCurrencyInstance()

        val ShortDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
        val MediumDateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

        const val SenderOrReceiverFormatDescriptor = "%sender"
        const val UsageFormatDescriptor = "%usage"

        const val MonthSingleDigitFormatDescriptor = "%m"
        private val MonthSingleDigitDateFormat = SimpleDateFormat(MonthSingleDigitFormatDescriptor.replace("%", "").toUpperCase())
        const val MonthTwoDigitFormatDescriptor = "%mm"
        private val MonthTwoDigitDateFormat = SimpleDateFormat(MonthTwoDigitFormatDescriptor.replace("%", "").toUpperCase())
        const val MonthAbbreviatedNameFormatDescriptor = "%mmm"
        private val MonthAbbreviatedNameDateFormat = SimpleDateFormat(MonthAbbreviatedNameFormatDescriptor.replace("%", "").toUpperCase())
        const val MonthNameFormatDescriptor = "%mmmm"
        private val MonthNameDateFormat = SimpleDateFormat(MonthNameFormatDescriptor.replace("%", "").toUpperCase())

        const val YearTwoDigitFormatDescriptor = "%yy"
        private val YearTwoDigitDateFormat = SimpleDateFormat(YearTwoDigitFormatDescriptor.replace("%", ""))
        const val YearFourDigitFormatDescriptor = "%yyyy"
        private val YearFourDigitDateFormat = SimpleDateFormat(YearFourDigitFormatDescriptor.replace("%", ""))

        const val DefaultDescriptionForCreatedDocuments = "$SenderOrReceiverFormatDescriptor - $UsageFormatDescriptor"

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


    fun saveOrUpdate(documents: List<Document>) {
        documents.forEach {
            saveOrUpdate(it)
        }
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
        document.items.forEach { item ->
            updateVat(item)
        }
    }

    fun updateVat(item: DocumentItem) {
        if (item.isValueAddedTaxRateSet) {
            if (item.isGrossAmountSet) {
                item.valueAddedTax = calculateVatFromTotalAmount(item)

                item.netAmount = item.grossAmount - item.valueAddedTax
            }
            else if (item.isNetAmountSet) {
                item.valueAddedTax = calculateVatFromNetAmount(item)

                item.grossAmount = item.netAmount + item.valueAddedTax
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


    fun saveOrUpdate(entityFilter: EntityFilter, updatedFilterDefinitions: List<Filter>?) {
        filterService.saveOrUpdate(entityFilter, updatedFilterDefinitions)
    }

    fun runAccountTransactionsFilterAndCreateNewDocuments(transactions: Collection<BankAccountTransaction>) {
        runFilterAndCreateNewDocuments(getAccountTransactionsEntityFilters(), transactions)
    }

    fun <T> runFilterAndCreateNewDocuments(entityFilters: List<EntityFilter>, collectionToFilter: Collection<T>) {
        entityFilters.forEach { entityFilter ->
            val itemsOnWithFilterApplies = filterService.filterCollection(entityFilter, collectionToFilter)

            // TODO: create ICreateDocumentFromEntity interface and a factory to get actual implementation, e. g. for BankAccountTransaction, Email, ...
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

        val documents = createDocumentsForTransactions(transactions, automaticallyCreatedFromFilter)

        saveOrUpdate(documents)
    }

    fun adjustBeforeAddingToExpendituresAndRevenues(transactions: Collection<BankAccountTransaction>) {
        val documents = createDocumentsForTransactions(transactions)

        documents.forEach {
            showEditDocumentWindow(it)
        }
    }

    private fun createDocumentsForTransactions(transactions: Collection<BankAccountTransaction>,
                                               automaticallyCreatedFromFilter: EntityFilter? = null): List<Document> {

        // don't create a second document from the same account transaction
        val transactionsWithNotYetCreatedDocuments = transactions.filter { it.createdDocument == null }

        return transactionsWithNotYetCreatedDocuments.map { transaction ->
            mapTransactionToDocumentAndSetReferencesOnIt(transaction, automaticallyCreatedFromFilter)
        }
    }

    fun updateDocumentsForEntityFilter(transactions: Collection<BankAccountTransaction>,
                                       automaticallyCreatedFromFilter: EntityFilter? = null): List<Document> {

        val updatedDocuments = transactions.map { transaction ->
            transaction.createdDocument?.let {
                return@map updateDocumentValuesAndSetReferencesOnIt(it, transaction, automaticallyCreatedFromFilter)
            }

            mapTransactionToDocumentAndSetReferencesOnIt(transaction, automaticallyCreatedFromFilter)
        }

        saveOrUpdate(updatedDocuments)

        return updatedDocuments
    }

    private fun mapTransactionToDocumentAndSetReferencesOnIt(transaction: BankAccountTransaction, automaticallyCreatedFromFilter: EntityFilter? = null): Document {
        val document = mapTransactionToDocument(transaction, automaticallyCreatedFromFilter)

        setDocumentReferences(document, transaction, automaticallyCreatedFromFilter)

        return document
    }

    private fun updateDocumentValuesAndSetReferencesOnIt(document: Document, transaction: BankAccountTransaction, automaticallyCreatedFromFilter: EntityFilter? = null): Document {
        setDocumentValues(document, transaction, automaticallyCreatedFromFilter)

        setDocumentReferences(document, transaction, automaticallyCreatedFromFilter)

        return document
    }

    private fun setDocumentReferences(document: Document, transaction: BankAccountTransaction, automaticallyCreatedFromFilter: EntityFilter? = null) {
        transaction.createdDocument = document
        document.createdFromAccountTransaction = transaction

        document.automaticallyCreatedFromFilter = automaticallyCreatedFromFilter
    }

    fun mapTransactionToDocument(transaction: BankAccountTransaction, automaticallyCreatedFromFilter: EntityFilter? = null): Document {

        val type = if (transaction.isDebit) DocumentType.Expenditure else DocumentType.Revenue

        val valueAddedTaxRate = automaticallyCreatedFromFilter?.valueAddedTaxRateForCreatedDocuments ?: getDefaultVatRateForUser()

        val document = Document(type, Math.abs(transaction.amount.toDouble()), valueAddedTaxRate)

        document.paymentDate = transaction.valueDate

        setDocumentValues(document, transaction, automaticallyCreatedFromFilter)

        return document
    }

    private fun setDocumentValues(document: Document, transaction: BankAccountTransaction, automaticallyCreatedFromFilter: EntityFilter?) {
        document.description = createDocumentDescription(transaction, automaticallyCreatedFromFilter)

        automaticallyCreatedFromFilter?.let {
            document.items.forEach { item ->
                item.valueAddedTaxRate = automaticallyCreatedFromFilter.valueAddedTaxRateForCreatedDocuments
            }
        }

        updateVat(document)
    }

    // visible for testing
    internal fun createDocumentDescription(transaction: BankAccountTransaction, automaticallyCreatedFromFilter: EntityFilter? = null): String {
        automaticallyCreatedFromFilter?.descriptionForCreatedDocuments?.let { descriptionFormat ->
            return descriptionFormat
                .replace(SenderOrReceiverFormatDescriptor, transaction.senderOrReceiverName)
                .replace(UsageFormatDescriptor, transaction.usage)
                .replace(MonthNameFormatDescriptor, MonthNameDateFormat.format(transaction.valueDate))
                .replace(MonthAbbreviatedNameFormatDescriptor, MonthAbbreviatedNameDateFormat.format(transaction.valueDate))
                .replace(MonthTwoDigitFormatDescriptor, MonthTwoDigitDateFormat.format(transaction.valueDate))
                .replace(MonthSingleDigitFormatDescriptor, MonthSingleDigitDateFormat.format(transaction.valueDate))
                .replace(YearFourDigitFormatDescriptor, YearFourDigitDateFormat.format(transaction.valueDate))
                .replace(YearTwoDigitFormatDescriptor, YearTwoDigitDateFormat.format(transaction.valueDate))
        }

        return transaction.senderOrReceiverName + " - " + transaction.usage
    }


    fun extractInvoiceData(invoiceFile: File): InvoiceData {
        val extractors = textExtractorRegistry.getAllExtractorsForFile(invoiceFile)

        var bestExtractedText: String? = null
        var firstException: Exception? = null

        for (extractor in extractors) {
            extractor.extractText(invoiceFile).text?.let { extractedText ->
                if (extractedText.isNotBlank()) { // if text extraction failed we continue on to next text extractor
                    if (extractedText.trim().length > (bestExtractedText?.length ?: -1)) {
                        bestExtractedText = extractedText.trim()
                    }

                    val extractedInvoiceData = invoiceDataExtractor.extractInvoiceData(extractedText)
                    if (extractedInvoiceData.couldExtractInvoiceData) {
                        return InvoiceData(invoiceFile, extractedText.trim(), extractedInvoiceData, null)
                    }
                    else if (firstException == null) { // if invoice data extraction failed we continue on to next text extractor
                        firstException = extractedInvoiceData.error
                    }
                }
            }
        }

        if (firstException == null && extractors.isEmpty()) {
            firstException = Exception("No suitable text extractors found for file '$invoiceFile'") // TODO: translate
        }

        return InvoiceData(invoiceFile, bestExtractedText, null, firstException)
    }


    /**
     * [roundDownNetAmount] is needed for revenues in Germany where you first have to round down the net amount and
     * calculate VAT from this value.
     */
    @JvmOverloads
    fun calculateVatFromNetAmount(item: DocumentItem, roundDownNetAmount: Boolean = false): Double {
        return vatCalculator.calculateVatFromNetAmount(item.netAmount, item.valueAddedTaxRate, roundDownNetAmount)
    }

    fun calculateVatFromTotalAmount(item: DocumentItem): Double {
        return vatCalculator.calculateVatFromTotalAmount(item.grossAmount, item.valueAddedTaxRate)
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

    fun showCreateRevenueWindow(extractedData: InvoiceData? = null) {
        val newRevenue = Document(DocumentType.Revenue)

        showEditDocumentWindow(newRevenue, extractedData)
    }

    fun showCreateExpenditureWindow(extractedData: InvoiceData? = null) {
        val newExpenditure = Document(DocumentType.Expenditure)

        showEditDocumentWindow(newExpenditure, extractedData)
    }

    fun showEditDocumentWindow(document: Document, extractedData: InvoiceData? = null) {
        router.showEditDocumentWindow(document, this, extractedData)
    }

}