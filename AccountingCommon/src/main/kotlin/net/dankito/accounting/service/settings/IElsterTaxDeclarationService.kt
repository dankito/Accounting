package net.dankito.accounting.service.settings

import net.dankito.accounting.data.model.tax.elster.ElsterTaxDeclarationSettings


interface IElsterTaxDeclarationService {

    val settings: ElsterTaxDeclarationSettings


    fun saveSettings()

}