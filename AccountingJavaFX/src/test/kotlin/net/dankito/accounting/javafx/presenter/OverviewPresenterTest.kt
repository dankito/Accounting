package net.dankito.accounting.javafx.presenter

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import net.dankito.accounting.data.dao.DocumentDao
import net.dankito.accounting.data.dao.DocumentItemDao
import net.dankito.accounting.data.dao.filter.EntityFilterDao
import net.dankito.accounting.data.dao.filter.FilterDao
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.filter.*
import net.dankito.accounting.data.model.settings.AppSettings
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.document.DocumentService
import net.dankito.accounting.service.filter.CollectionFilter
import net.dankito.accounting.service.filter.FilterService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.accounting.util.db.DatabaseBasedTest
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.events.RxEventBus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*
import java.util.concurrent.atomic.AtomicReference


class OverviewPresenterTest : DatabaseBasedTest() {

    companion object {
        private const val FilterName = "Test filter"

        private const val StartsWithFilterText = "Racketeering Ltd. telefon charges"

        private const val CountTransactionsStartsWith = 3


        init {
            Locale.setDefault(Locale.ENGLISH) // so that month name gets formatted in English
        }
    }


    private val mockedTodayValue = AtomicReference<LocalDate>(LocalDate.now())

    private val appSettingsMock = mock(AppSettings::class.java)

    private val routerMock = mock(Router::class.java)


    private val eventBus = RxEventBus()

    private val documentDao = DocumentDao(entityManager)

    private val documentService = DocumentService(documentDao, DocumentItemDao(entityManager), eventBus)

    private val settingsServiceMock = mock(ISettingsService::class.java)

    private val bankAccountServiceMock = mock(IBankAccountService::class.java)

    private val filterService = FilterService(CollectionFilter(), EntityFilterDao(entityManager), FilterDao(entityManager))


    private val underTest :OverviewPresenter


    init {
        doReturn(AccountingPeriod.Monthly).`when`(appSettingsMock).accountingPeriod

        doReturn(appSettingsMock).`when`(settingsServiceMock).appSettings

        underTest = object : OverviewPresenter(documentService, settingsServiceMock, bankAccountServiceMock, filterService, eventBus, routerMock) {

            override fun getToday(): LocalDate {
                return mockedTodayValue.get()
            }

        }
    }




    @Test
    fun filterCollection_startsWith() {

        // given
        val startsWithFilter = Filter(FilterType.String, FilterOption.StartsWith, true, StartsWithFilterText,
            BankAccountTransaction::class.java, AccountTransactionProperty.Usage.propertyName)
        val entityFilter = EntityFilter(FilterName, BankAccountTransaction::class.java, 0f, OverviewPresenter.DefaultDescriptionForCreatedDocuments, listOf(startsWithFilter))
        underTest.saveOrUpdate(entityFilter)

        val accountTransactions = createTransactions(countTransactonsStartsWith = CountTransactionsStartsWith)
        assertThat(accountTransactions.size).isGreaterThan(CountTransactionsStartsWith)

        doAnswer { answer ->
            val callback = answer.getArgument(0) as ((List<BankAccountTransaction>) -> Unit)
            callback(accountTransactions)
        }.`when`(bankAccountServiceMock).updateAccountsTransactionsAsync(any())


        assertThat(documentDao.getAll()).isEmpty()


        // when
        eventBus.post(BankAccountTransactionsUpdatedEvent(accountTransactions))


        // then
        val result = documentDao.getAll()

        assertThat(result).hasSize(CountTransactionsStartsWith)

        result.forEach { createdDocument ->
            assertThat(createdDocument.isPersisted()).isTrue()
            assertThat(createdDocument.description).contains(StartsWithFilterText)

            assertThat(createdDocument.createdFromAccountTransaction).isNotNull

            assertThat(createdDocument.automaticallyCreatedFromFilter).isNotNull
            assertThat(createdDocument.automaticallyCreatedFromFilter?.isPersisted()).isTrue()
            assertThat(createdDocument.automaticallyCreatedFromFilter).isEqualTo(entityFilter)
        }
    }


    @Test
    fun getCurrentAccountingPeriodStartDate_Monthly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 6, 1).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 4, 1).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 2, 29))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2020, 2, 1).asUtilDate())
    }


    @Test
    fun getCurrentAccountingPeriodStartDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 4, 1).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 4, 1).asUtilDate())
    }


    @Test
    fun getCurrentAccountingPeriodStartDate_Annually_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Annually_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Annually_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2020, 2, 29))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2020, 1, 1).asUtilDate())
    }


    @Test
    fun getCurrentAccountingPeriodEndDate_Monthly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 6, 30).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 4, 30).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 2, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2020, 2, 29).asUtilDate())
    }


    @Test
    fun getCurrentAccountingPeriodEndDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 6, 30).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 6, 30).asUtilDate())
    }


    @Test
    fun getCurrentAccountingPeriodEndDate_Annually_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 12, 31).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Annually_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 12, 31).asUtilDate())
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Annually_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2020, 2, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2020, 12, 31).asUtilDate())
    }


    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 5, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 3, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_EndOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_StartOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 2, 29))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2020, 1, 1).asUtilDate())
    }


    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_EndOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 10, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_StartOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 10, 1).asUtilDate())
    }


    @Test
    fun getPreviousAccountingPeriodStartDate_Annually_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 1, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Annually_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 1, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Annually_EndOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 1, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 1, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Annually_StartOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 1, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 1, 1).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Annually_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2020, 2, 29))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 1, 1).asUtilDate())
    }


    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 3, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_EndOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_StartOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 3, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2020, 2, 29).asUtilDate())
    }


    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 3, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 3, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_EndOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_StartOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }


    @Test
    fun getPreviousAccountingPeriodEndDate_Annually_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Annually_EndOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 1, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Annually_StartOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2019, 1, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2018, 12, 31).asUtilDate())
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Annually_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Annually
        mockedTodayValue.set(LocalDate.of(2020, 3, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(LocalDate.of(2019, 12, 31).asUtilDate())
    }


    @Test
    fun createDocumentDescription_Sender() {

        // given
        val usage = "A shitload of money"
        val sender = "Your employer"
        val transaction = createTransaction(1.0, usage, sender)
        val entityFilter = createEntityFilter(OverviewPresenter.SenderOrReceiverFormatDescriptor)

        // when
        val result = underTest.createDocumentDescription(transaction, entityFilter)

        // then
        assertThat(result).isEqualTo(sender)
    }

    @Test
    fun createDocumentDescription_Usage() {

        // given
        val usage = "A shitload of money"
        val sender = "Your employer"
        val transaction = createTransaction(1.0, usage, sender)
        val entityFilter = createEntityFilter(OverviewPresenter.UsageFormatDescriptor)

        // when
        val result = underTest.createDocumentDescription(transaction, entityFilter)

        // then
        assertThat(result).isEqualTo(usage)
    }

    @Test
    fun createDocumentDescription_SenderAndUsage() {

        // given
        val usage = "A shitload of money"
        val sender = "Your employer"
        val transaction = createTransaction(1.0, usage, sender)
        val entityFilter = createEntityFilter(OverviewPresenter.SenderOrReceiverFormatDescriptor + " - " + OverviewPresenter.UsageFormatDescriptor)

        // when
        val result = underTest.createDocumentDescription(transaction, entityFilter)

        // then
        assertThat(result).isEqualTo(sender + " - " + usage)
    }

    @Test
    fun createDocumentDescription_ShortMonthAndShortYear() {

        // given
        val year = 2020
        val month = 1
        val valueDate = LocalDate.of(year, month, 31).asUtilDate()
        val transaction = createTransaction(1.0, "", "", valueDate)
        val entityFilter = createEntityFilter("Pay " + OverviewPresenter.MonthSingleDigitFormatDescriptor + " " + OverviewPresenter.YearTwoDigitFormatDescriptor)

        // when
        val result = underTest.createDocumentDescription(transaction, entityFilter)

        // then
        assertThat(result).isEqualTo("Pay " + month + " 20")
    }

    @Test
    fun createDocumentDescription_LongMonthAndLongYear() {

        // given
        val year = 2020
        val month = 1
        val valueDate = LocalDate.of(year, month, 31).asUtilDate()
        val transaction = createTransaction(1.0, "", "", valueDate)
        val entityFilter = createEntityFilter("Pay " + OverviewPresenter.MonthTwoDigitFormatDescriptor + " " + OverviewPresenter.YearFourDigitFormatDescriptor)

        // when
        val result = underTest.createDocumentDescription(transaction, entityFilter)

        // then
        assertThat(result).isEqualTo("Pay 0" + month + " " + year)
    }

    @Test
    fun createDocumentDescription_MonthNameAndLongYear() {

        // given
        val year = 2020
        val month = 1
        val valueDate = LocalDate.of(year, month, 31).asUtilDate()
        val transaction = createTransaction(1.0, "", "", valueDate)
        val entityFilter = createEntityFilter("Pay " + OverviewPresenter.MonthNameFormatDescriptor + " " + OverviewPresenter.YearFourDigitFormatDescriptor)

        // when
        val result = underTest.createDocumentDescription(transaction, entityFilter)

        // then
        assertThat(result).isEqualTo("Pay January " + year)
    }


    private fun createTransactions(countTransactonsStartsWith: Int = CountTransactionsStartsWith): List<BankAccountTransaction> {
        val collectionToFilter = mutableListOf<BankAccountTransaction>()

        for (i in 0 until countTransactonsStartsWith) {
            collectionToFilter.add(createTransaction(35.0, StartsWithFilterText + " for month $i"))
        }

        for (i in 0 until 10) {
            collectionToFilter.add(createTransaction(i * 123.0, "noise$i"))
        }

        return collectionToFilter
    }

    private fun createTransaction(amount: Double, usage: String, senderOrReceiverName: String = "", valueDate: Date = Date()): BankAccountTransaction {
        return BankAccountTransaction(
            BigDecimal.valueOf(amount), usage, true, senderOrReceiverName,
            "", "", valueDate, "", "", BigDecimal.ZERO, BankAccount("", "", "")
        )
    }

    private fun createEntityFilter(descriptionForCreatedDocuments: String): EntityFilter {
        return EntityFilter("", BankAccountTransaction::class.java, 0f, descriptionForCreatedDocuments, listOf())
    }

}