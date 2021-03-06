package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.event.ApplicationClosingEvent
import net.dankito.accounting.data.model.tax.FederalState
import net.dankito.accounting.data.model.tax.TaxOffice
import net.dankito.accounting.data.model.tax.elster.ElsterTaxDeclarationSettings
import net.dankito.accounting.service.tax.IFederalStateService
import net.dankito.accounting.service.tax.ITaxOfficeService
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import net.dankito.tax.elster.ElsterClient
import net.dankito.tax.elster.model.*
import net.dankito.utils.IThreadPool
import net.dankito.utils.events.IEventBus
import org.slf4j.LoggerFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Don't create ElsterTaxPresenter twice as creating the contained ERiC (via ElsterClient) a second time would
 * result in a JVM crash.
 */
class ElsterTaxPresenter(private val settingsService: IElsterTaxDeclarationService,
                         private val federalStateService: IFederalStateService,
                         private val taxOfficeService: ITaxOfficeService,
                         eventBus: IEventBus,
                         private val threadPool: IThreadPool,
                         logFilesFolder: File): AutoCloseable {

    companion object {
        private val DateToYearFormatter = SimpleDateFormat("yyyy")
        private val DateToMonthFormatter = SimpleDateFormat("MM")
        private val PeriodStartDateFormatter = SimpleDateFormat("dd.MM.yyyy")

        private val log = LoggerFactory.getLogger(ElsterTaxPresenter::class.java)
    }

    /**
     * Don't create an ElsterClient - and therefore this class - a second time, even not after calling close(), as
     * creating the contained ERiC a second time crashes the JVM (even after thoroughly cleaning it up before).
     */
    private val client = ElsterClient(logFilesFolder)

    private var persistedFederalStatesProperty = federalStateService.getAll()


    val settings = settingsService.settings

    val persistedFederalStates: List<FederalState>
        get() = ArrayList(persistedFederalStatesProperty)


    init {
        eventBus.subscribe(ApplicationClosingEvent::class.java) {
            close()
        }
    }

    override fun close() {
        client.close()
    }


    fun saveSettings() {
        settingsService.saveSettings()
    }

    fun getAllTaxOfficesAsync(callback: (List<FederalState>) -> Unit) {
        threadPool.runAsync {
            val federalStates = getAllTaxOffices()

            callback(federalStates)
        }
    }

    private fun getAllTaxOffices(): List<FederalState> {
        try {
            val federalStates = mapToFederalStates(client.getFinanzämter())

            val previousFederalStates = ArrayList(persistedFederalStatesProperty)

            taxOfficeService.saveOrUpdate(federalStates.flatMap { it.taxOffices })

            federalStateService.saveOrUpdate(federalStates)

            persistedFederalStatesProperty = federalStates

            previousFederalStates.removeAll(federalStates)
            federalStateService.delete(previousFederalStates)

            return federalStates
        } catch (e: Exception) {
            log.info("Could not get German tax offices", e)
        }

        return listOf()
    }

    private fun mapToFederalStates(orderedFinanzaemter: Map<Bundesland, List<Finanzamt>>): List<FederalState> {
        return orderedFinanzaemter.map { entry ->
            mapToFederalState(entry.key, entry.value)
        }
    }

    private fun mapToFederalState(bundesland: Bundesland, finanzaemterForBundesland: List<Finanzamt>): FederalState {
        val taxOffices = finanzaemterForBundesland.map { mapToTaxOffice(it) }

        getExistingFederalState(bundesland)?.let { federalState ->
            federalState.name = bundesland.name
            federalState.setTaxOffices(taxOffices)

            return federalState
        }

        return FederalState(bundesland.name, bundesland.elsterFinanzamtLandId, taxOffices)
    }

    private fun getExistingFederalState(bundesland: Bundesland): FederalState? {
        return persistedFederalStatesProperty.firstOrNull { it.federalStateId == bundesland.elsterFinanzamtLandId }
    }

    private fun mapToTaxOffice(finanzamt: Finanzamt): TaxOffice {
        getExistingTaxOffice(finanzamt)?.let { taxOffice ->
            taxOffice.name = finanzamt.name

            return taxOffice
        }

        return TaxOffice(finanzamt.name, finanzamt.finanzamtsnummer)
    }

    private fun getExistingTaxOffice(finanzamt: Finanzamt): TaxOffice? {
        return persistedFederalStatesProperty.flatMap { it.taxOffices }
                                             .firstOrNull { it.taxOfficeId == finanzamt.finanzamtsnummer }
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


    fun getHerstellerID(): String? {
        ElsterTaxDeclarationSettings::class.java.classLoader.getResourceAsStream("config/elster.properties")?.let { inputStream ->
            val properties = Properties()

            properties.load(inputStream)

            return properties.getProperty("tax.elster.herstellerID", null)
        }

        return null
    }

}