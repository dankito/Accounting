package net.dankito.accounting.service.filter

import net.dankito.accounting.data.dao.filter.EntityFilterDao
import net.dankito.accounting.data.dao.filter.FilterDao
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.data.model.filter.*
import net.dankito.accounting.service.util.db.DatabaseBasedTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.util.*

class FilterServiceTest : DatabaseBasedTest() {

    companion object {
        private const val StartsWithFilterText = "Racketeering Ltd. telefon charges"

        private const val CountTransactonsStartsWith = 3
    }


    private val collectionFilter = CollectionFilter()

    private val entityFilterDao = EntityFilterDao(entityManager)

    private val filterDao = FilterDao(entityManager)


    private val underTest = FilterService(collectionFilter, entityFilterDao, filterDao)


    @Test
    fun filterCollection_startsWith() {

        // given
        val startsWithFilter = Filter(FilterType.String, FilterOption.StartsWith, true, StartsWithFilterText,
            BankAccountTransaction::class.java, AccountTransactionProperty.Usage.propertyName)
        val entityFilter = EntityFilter(BankAccountTransaction::class.java, listOf(startsWithFilter))
        underTest.saveOrUpdate(entityFilter)

        val collectionToFilter = createTransactions(countTransactonsStartsWith = CountTransactonsStartsWith)
        assertThat(collectionToFilter.size).isGreaterThan(CountTransactonsStartsWith)


        // when
        val result = underTest.filterCollection(entityFilter, collectionToFilter)


        // then
        assertThat(result).hasSize(CountTransactonsStartsWith)

    }


    private fun createTransactions(countTransactonsStartsWith: Int = CountTransactonsStartsWith): List<BankAccountTransaction> {
        val collectionToFilter = mutableListOf<BankAccountTransaction>()

        for (i in 0 until countTransactonsStartsWith) {
            collectionToFilter.add(createTransaction(35.0, StartsWithFilterText + " for month $i"))
        }

        for (i in 0 until 10) {
            collectionToFilter.add(createTransaction(i * 123.0, "noise$i"))
        }

        return collectionToFilter
    }

    private fun createTransaction(amount: Double, usage: String, senderOrReceiverName: String = ""): BankAccountTransaction {
        return BankAccountTransaction(BigDecimal.valueOf(amount), usage, true, senderOrReceiverName,
            "", "", Date(), "", "", BigDecimal.ZERO, BankAccount()
        )
    }

}