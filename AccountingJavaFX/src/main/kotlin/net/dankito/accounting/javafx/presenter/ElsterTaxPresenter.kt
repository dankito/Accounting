package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.javafx.windows.person.EditPersonWindow
import net.dankito.accounting.service.person.IPersonService
import net.dankito.tax.elster.ElsterClient
import net.dankito.tax.elster.model.*
import net.dankito.utils.IThreadPool
import java.text.SimpleDateFormat
import java.util.*


class ElsterTaxPresenter(private val personService: IPersonService, private val threadPool: IThreadPool): AutoCloseable {

    companion object {
        private val DateToYearFormatter = SimpleDateFormat("yyyy")
        private val DateToMonthFormatter = SimpleDateFormat("MM")
        private val PeriodStartDateFormatter = SimpleDateFormat("dd.MM.yyyy")
    }

    /**
     * Don't create an ElsterClient - and therefore this class - a second time, even not after calling close(), as
     * creating the contained ERiC a second time crashes the JVM (even after thoroughly cleaning it up before).
     */
    private val client = ElsterClient()


    override fun close() {
        client.close()
    }


    fun getAllPersons(): List<Person> {
        return personService.getAll()
    }

    fun getAllFinanzaemterAsync(callback: (Map<Bundesland, List<Finanzamt>>) -> Unit) {
        threadPool.runAsync {
            callback(client.getFinanzämter())
        }
    }


    fun getYearFromPeriod(dateInPeriod: Date): Steuerjahr? {
        val year = DateToYearFormatter.format(dateInPeriod)

        Steuerjahr.values().forEach { steuerjahr ->
            if (steuerjahr.jahr == year) {
                return steuerjahr
            }
        }

        return null
    }

    fun getVoranmeldungszeitrumFromPeriod(dateInPeriod: Date, accountingPeriod: AccountingPeriod): Voranmeldungszeitraum? {
        val month = DateToMonthFormatter.format(dateInPeriod)

        if (accountingPeriod == AccountingPeriod.Monthly) {
            Voranmeldungszeitraum.values().forEach { voranmeldungszeitraum ->
                if (voranmeldungszeitraum.ziffer == month) {
                    return voranmeldungszeitraum
                }
            }
        }
        else if (accountingPeriod == AccountingPeriod.Quarterly) {
            val monthInt = month.toInt()

            if (monthInt > 9) {
                return Voranmeldungszeitraum.IV_Kalendervierteljahr
            }
            else if (monthInt > 6) {
                return Voranmeldungszeitraum.III_Kalendervierteljahr
            }
            else if (monthInt > 3) {
                return Voranmeldungszeitraum.II_Kalendervierteljahr
            }
            else {
                return Voranmeldungszeitraum.I_Kalendervierteljahr
            }
        }

        return null
    }

    fun getAccountingPeriodFromZeitraum(zeitraum: Voranmeldungszeitraum): AccountingPeriod {
        return when (zeitraum) {
            Voranmeldungszeitraum.I_Kalendervierteljahr,
            Voranmeldungszeitraum.II_Kalendervierteljahr,
            Voranmeldungszeitraum.III_Kalendervierteljahr,
            Voranmeldungszeitraum.IV_Kalendervierteljahr -> AccountingPeriod.Quarterly
            else -> AccountingPeriod.Monthly
        }
    }

    fun getSelectedPeriodStartDate(jahr: Steuerjahr, zeitraum: Voranmeldungszeitraum): Date {
        val month =
                when (zeitraum) {
                    Voranmeldungszeitraum.I_Kalendervierteljahr -> "1"
                    Voranmeldungszeitraum.II_Kalendervierteljahr -> "4"
                    Voranmeldungszeitraum.III_Kalendervierteljahr -> "7"
                    Voranmeldungszeitraum.IV_Kalendervierteljahr -> "10"
                    else -> zeitraum.ziffer
                }

        return PeriodStartDateFormatter.parse("01.${month}.${jahr.jahr}")
    }


    fun createUmsatzsteuerVoranmeldungXmlFile(data: UmsatzsteuerVoranmeldung): ElsterFileCreationTransactionResult {
        return client.createAndValidateUmsatzsteuerVoranmeldungXmlFile(data)
    }

    fun makeUmsatzsteuerVoranmeldung(data: UmsatzsteuerVoranmeldung): ElsterTransactionResult {
        return client.makeUmsatzsteuerVoranmeldung(data)
    }


    fun showCreatePersonWindow(createdPersonCallback: (Person?) -> Unit) {
        val newPerson = Person()

        showEditPersonWindow(newPerson) { userDidSavePerson ->
            createdPersonCallback( if (userDidSavePerson) newPerson else null )
        }
    }

    fun showEditPersonWindow(person: Person, userDidEditPersonCallback: (Boolean) -> Unit) {
        EditPersonWindow(person, personService, userDidEditPersonCallback).show()
    }

}