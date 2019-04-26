package net.dankito.accounting.service.settings

import net.dankito.accounting.data.dao.IElsterTaxDeclarationSettingsDao
import net.dankito.accounting.data.model.tax.elster.ElsterTaxDeclarationSettings
import org.slf4j.LoggerFactory


open class ElsterTaxDeclarationService(protected val dao: IElsterTaxDeclarationSettingsDao
) : IElsterTaxDeclarationService {

    companion object {
        private val log = LoggerFactory.getLogger(ElsterTaxDeclarationService::class.java)
    }


    override val settings: ElsterTaxDeclarationSettings = retrieveOrCreateAppSettings()


    override fun saveSettings() {
        dao.saveOrUpdate(settings)
    }


    protected open fun retrieveOrCreateAppSettings(): ElsterTaxDeclarationSettings {
        val all = dao.getAll()

        if (all.isEmpty()) {
            val newSettings = ElsterTaxDeclarationSettings()
            dao.saveOrUpdate(newSettings)

            return newSettings
        }
        else {
            if (all.size > 1) { // should never be the case that all contains more than one ElsterTaxDeclarationSettings instance
                log.warn("There are more than on ElsterTaxDeclarationSettings persisted. Returning first one: ${all[0]}")
            }

            return all[0]
        }
    }

}