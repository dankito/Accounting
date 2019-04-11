package net.dankito.accounting.javafx.windows.mainwindow

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.utils.datetime.DateConvertUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference


class OverviewPresenterTest {

    private val mockedTodayValue = AtomicReference<LocalDate>(LocalDate.now())

    private val documentsServiceMock = mock(IDocumentService::class.java)

    private val routerMock = mock(Router::class.java)


    private val underTest = object : OverviewPresenter(documentsServiceMock, routerMock) {

        override fun getToday(): LocalDate {
            return mockedTodayValue.get()
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
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 6, 1)))
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 4, 1)))
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 2, 29))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2020, 2, 1)))
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 4, 1)))
    }

    @Test
    fun getCurrentAccountingPeriodStartDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 4, 1)))
    }


    @Test
    fun getCurrentAccountingPeriodEndDate_Monthly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 6, 30)))
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 4, 30)))
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 2, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2020, 2, 29)))
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 6, 30)))
    }

    @Test
    fun getCurrentAccountingPeriodEndDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getCurrentAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 6, 30)))
    }


    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 5, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 3, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_EndOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 12, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_StartOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 12, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 2, 29))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2020, 1, 1)))
    }


    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 1, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 1, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_EndOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 10, 1)))
    }

    @Test
    fun getPreviousAccountingPeriodStartDate_Quarterly_StartOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodStartDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 10, 1)))
    }


    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 3, 31)))
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_EndOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 12, 31)))
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_StartOfJanuary() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2019, 1, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 12, 31)))
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Monthly_LeapYear() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Monthly
        mockedTodayValue.set(LocalDate.of(2020, 3, 31))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2020, 2, 29)))
    }


    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_EndOfJune() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 6, 30))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 3, 31)))
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_StartOfApril() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 4, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2019, 3, 31)))
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_EndOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 12, 31)))
    }

    @Test
    fun getPreviousAccountingPeriodEndDate_Quarterly_StartOfMarch() {

        // given
        underTest.accountingPeriod = AccountingPeriod.Quarterly
        mockedTodayValue.set(LocalDate.of(2019, 3, 1))

        // when
        val result = underTest.getPreviousAccountingPeriodEndDate()

        // then
        assertThat(result).isEqualTo(DateConvertUtils.asUtilDate(LocalDate.of(2018, 12, 31)))
    }

}