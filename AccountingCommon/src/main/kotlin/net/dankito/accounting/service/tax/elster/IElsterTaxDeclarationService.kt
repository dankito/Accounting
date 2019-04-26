package net.dankito.accounting.service.tax.elster

import net.dankito.accounting.data.model.tax.elster.ElsterTaxDeclarationSettings


interface IElsterTaxDeclarationService {

    val settings: ElsterTaxDeclarationSettings


    fun saveSettings()

}