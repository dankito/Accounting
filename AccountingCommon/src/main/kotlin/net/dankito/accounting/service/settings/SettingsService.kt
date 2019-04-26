package net.dankito.accounting.service.settings

import net.dankito.accounting.data.dao.IAppSettingsDao
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.settings.AppSettings
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import org.slf4j.LoggerFactory


open class SettingsService(protected val appSettingsDao: IAppSettingsDao,
                           protected val elsterTaxDeclarationService: IElsterTaxDeclarationService
) : ISettingsService {

    companion object {
        private val log = LoggerFactory.getLogger(SettingsService::class.java)
    }


    override val appSettings: AppSettings = retrieveOrCreateAppSettings()


    override fun saveAppSettings() {
        appSettingsDao.saveOrUpdate(appSettings)
    }


    protected open fun retrieveOrCreateAppSettings(): AppSettings {
        val all = appSettingsDao.getAll()

        if (all.isEmpty()) {
            val newAppSettings = AppSettings(AccountingPeriod.Monthly, elsterTaxDeclarationService.settings)

            appSettingsDao.saveOrUpdate(newAppSettings)

            return newAppSettings
        }
        else {
            if (all.size > 1) { // should never be the case that all contains more than one AppSettings instance
                log.warn("There are more than on AppSettings persisted. Returning first one: ${all[0]}")
            }

            return all[0]
        }
    }

}